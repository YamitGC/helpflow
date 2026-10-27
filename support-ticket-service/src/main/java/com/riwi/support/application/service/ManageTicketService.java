package com.riwi.support.application.service;

import com.riwi.support.application.mapper.TicketApplicationMapper;
import com.riwi.support.domain.exception.AccessDeniedDomainException;
import com.riwi.support.domain.exception.BusinessRuleException;
import com.riwi.support.domain.exception.InvalidStateTransitionException;
import com.riwi.support.domain.exception.ResourceNotFoundException;
import com.riwi.support.domain.model.Technician;
import com.riwi.support.domain.model.Ticket;
import com.riwi.support.domain.model.TicketHistory;
import com.riwi.support.domain.model.TicketPriority;
import com.riwi.support.domain.model.TicketStatus;
import com.riwi.support.domain.model.User;
import com.riwi.support.domain.port.input.ManageTicketUseCase;
import com.riwi.support.domain.port.output.TechnicianDispatchPort;
import com.riwi.support.domain.port.output.TechnicianRepositoryPort;
import com.riwi.support.domain.port.output.TicketRepositoryPort;
import com.riwi.support.domain.port.output.UserRepositoryPort;
import com.riwi.support.infrastructure.metrics.HelpFlowMetrics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ManageTicketService implements ManageTicketUseCase {

    private final TicketRepositoryPort ticketRepositoryPort;
    private final UserRepositoryPort userRepositoryPort;
    private final TechnicianRepositoryPort technicianRepositoryPort;
    private final TechnicianDispatchPort technicianDispatchPort;
    private final TicketApplicationMapper ticketMapper;
    private final HelpFlowMetrics metrics;

    @Override
    @Transactional
    public Ticket createTicket(CreateTicketCommand command) {
        log.info("Iniciando creación de ticket para el usuario ID: {}", command.solicitanteId());

        User solicitante = userRepositoryPort.findById(command.solicitanteId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario solicitante no encontrado con ID: " + command.solicitanteId()));

        if (!solicitante.isActivo()) {
            throw new BusinessRuleException("El usuario solicitante no está ACTIVO en el sistema");
        }

        LocalDateTime ahora = LocalDateTime.now();
        int slaHoras = command.prioridad() != null ? command.prioridad().getSlaHours() : TicketPriority.MEDIA.getSlaHours();
        LocalDateTime fechaLimiteSla = ahora.plusHours(slaHoras);

        Ticket ticket = ticketMapper.toDomain(command, solicitante, ahora, fechaLimiteSla);
        Ticket ticketGuardado = ticketRepositoryPort.save(ticket);

        TicketHistory historialCreacion = ticketMapper.toHistory(
                ticketGuardado,
                solicitante.getId(),
                solicitante.getNombre(),
                null,
                TicketStatus.ABIERTO,
                "Ticket creado en estado ABIERTO.",
                ahora
        );
        ticketGuardado.getHistoriales().add(historialCreacion);
        ticketRepositoryPort.save(ticketGuardado);

        // Registrar métrica de observabilidad
        metrics.incrementCreatedTickets(command.categoria(), command.prioridad());

        // Intentar autoasignación
        try {
            return autoAssignTicket(ticketGuardado.getId());
        } catch (Exception e) {
            log.warn("No se pudo autoasignar el ticket ID {} inmediatamente: {}", ticketGuardado.getId(), e.getMessage());
            return ticketGuardado;
        }
    }

    @Override
    @Transactional
    public Ticket autoAssignTicket(Long ticketId) {
        Ticket ticket = ticketRepositoryPort.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket no encontrado con ID: " + ticketId));

        if (ticket.getEstado() != TicketStatus.ABIERTO) {
            log.info("El ticket ID {} ya no está en estado ABIERTO para autoasignación", ticketId);
            return ticket;
        }

        TechnicianDispatchPort.DispatchResult dispatch = technicianDispatchPort.requestTechnician(
                ticket.getId(),
                ticket.getCategoria(),
                ticket.getPrioridad()
        );

        log.info("Dispatch retornó sugerencia: técnico código {} con disponibilidad {}",
                dispatch.tecnicoId(), dispatch.disponibilidad());

        Technician tecnicoSeleccionado = findBestTechnician(ticket, dispatch.tecnicoId());

        if (tecnicoSeleccionado == null) {
            log.warn("No hay técnicos disponibles que cumplan los criterios para el ticket ID {}", ticketId);
            return ticket;
        }

        return assignTicket(
                ticket.getId(),
                tecnicoSeleccionado.getId(),
                null,
                "Asignación automática basada en dispatch (" + dispatch.disponibilidad() + ", est: " + dispatch.tiempoEstimadoHoras() + "h)"
        );
    }

    @Override
    @Transactional
    public Ticket assignTicket(Long ticketId, Long tecnicoId, Long adminOrSystemUserId, String motivo) {
        Ticket ticket = ticketRepositoryPort.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket no encontrado con ID: " + ticketId));

        Technician tecnico = technicianRepositoryPort.findById(tecnicoId)
                .orElseThrow(() -> new ResourceNotFoundException("Técnico no encontrado con ID: " + tecnicoId));

        if (!tecnico.isActivo()) {
            throw new BusinessRuleException("El técnico seleccionado no está ACTIVO");
        }

        if (!tecnico.isEspecialidadCompatible(ticket.getCategoria())) {
            throw new BusinessRuleException(String.format("El técnico %s tiene especialidad %s incompatible con categoría %s",
                    tecnico.getNombre(), tecnico.getEspecialidad(), ticket.getCategoria()));
        }

        long ticketsActivos = ticketRepositoryPort.countActiveTicketsByTecnicoId(tecnico.getId());
        int maxPermitido = tecnico.getMaxTicketsActivos() != null ? tecnico.getMaxTicketsActivos() : 5;
        if (ticketsActivos >= maxPermitido) {
            throw new BusinessRuleException(String.format("El técnico %s ha alcanzado su carga máxima (%d tickets activos)",
                    tecnico.getNombre(), maxPermitido));
        }

        TicketStatus anterior = ticket.getEstado();
        ticket.setTecnicoAsignado(tecnico);
        ticket.setEstado(TicketStatus.ASIGNADO);

        String usuarioNombre = "SISTEMA";
        if (adminOrSystemUserId != null) {
            User admin = userRepositoryPort.findById(adminOrSystemUserId).orElse(null);
            if (admin != null) usuarioNombre = admin.getNombre();
        }

        TicketHistory historial = ticketMapper.toHistory(
                ticket,
                adminOrSystemUserId,
                usuarioNombre,
                anterior,
                TicketStatus.ASIGNADO,
                motivo != null ? motivo : "Ticket asignado al técnico: " + tecnico.getNombre(),
                LocalDateTime.now()
        );

        ticket.getHistoriales().add(historial);
        return ticketRepositoryPort.save(ticket);
    }

    @Override
    @Transactional
    public Ticket updateTicketStatus(Long ticketId, TicketStatus nuevoEstado, String observaciones, Long usuarioAccionId) {
        Ticket ticket = ticketRepositoryPort.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket no encontrado con ID: " + ticketId));

        User usuarioAccion = userRepositoryPort.findById(usuarioAccionId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + usuarioAccionId));

        TicketStatus estadoActual = ticket.getEstado();

        if (!estadoActual.canTransitionTo(nuevoEstado)) {
            throw new InvalidStateTransitionException(
                    String.format("Transición inválida de %s a %s para el ticket ID %d", estadoActual, nuevoEstado, ticketId));
        }

        LocalDateTime ahora = LocalDateTime.now();
        if (nuevoEstado == TicketStatus.EN_PROGRESO && ticket.getFechaPrimeraRespuesta() == null) {
            ticket.setFechaPrimeraRespuesta(ahora);
            metrics.recordFirstResponseTime(Duration.between(ticket.getFechaCreacion(), ahora));
        }
        if (nuevoEstado == TicketStatus.RESUELTO || nuevoEstado == TicketStatus.CERRADO) {
            if (ticket.getFechaResolucion() == null) {
                ticket.setFechaResolucion(ahora);
                metrics.recordResolutionTime(Duration.between(ticket.getFechaCreacion(), ahora));
                if (ticket.isVencido()) {
                    metrics.incrementExpiredTickets();
                }
            }
        }

        ticket.setEstado(nuevoEstado);

        TicketHistory historial = ticketMapper.toHistory(
                ticket,
                usuarioAccion.getId(),
                usuarioAccion.getNombre(),
                estadoActual,
                nuevoEstado,
                observaciones,
                ahora
        );

        ticket.getHistoriales().add(historial);
        return ticketRepositoryPort.save(ticket);
    }

    @Override
    @Transactional
    public void checkAndEscalateTicketsNearingSlaBreach() {
        List<Ticket> candidatos = ticketRepositoryPort.findTicketsNearingSlaBreach();
        LocalDateTime ahora = LocalDateTime.now();

        for (Ticket ticket : candidatos) {
            TicketPriority prioridadOriginal = ticket.getPrioridad();
            TicketPriority nuevaPrioridad = switch (prioridadOriginal) {
                case BAJA -> TicketPriority.MEDIA;
                case MEDIA -> TicketPriority.ALTA;
                case ALTA, CRITICA -> TicketPriority.CRITICA;
            };

            if (prioridadOriginal != nuevaPrioridad) {
                ticket.setPrioridad(nuevaPrioridad);

                TicketHistory historialEscalamiento = ticketMapper.toHistory(
                        ticket,
                        null,
                        "SISTEMA_AUTO_ESCALA",
                        ticket.getEstado(),
                        ticket.getEstado(),
                        String.format("Escalamiento automático por proximidad a límite SLA (%s -> %s)",
                                prioridadOriginal, nuevaPrioridad),
                        ahora
                );

                ticket.getHistoriales().add(historialEscalamiento);
                ticketRepositoryPort.save(ticket);
                log.info("Ticket ID {} escalado automáticamente a prioridad {}", ticket.getId(), nuevaPrioridad);
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Ticket getTicketById(Long ticketId, Long currentUserId, boolean isAdminOrTech) {
        Ticket ticket = ticketRepositoryPort.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket no encontrado con ID: " + ticketId));

        if (!isAdminOrTech && !ticket.getSolicitante().getId().equals(currentUserId)) {
            metrics.incrementAccessDenied();
            throw new AccessDeniedDomainException("No tiene permisos para consultar este ticket.");
        }

        return ticket;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Ticket> getTicketsForCurrentUser(Long currentUserId, boolean isAdmin, boolean isTecnico) {
        if (isAdmin) {
            return ticketRepositoryPort.findAll();
        }
        if (isTecnico) {
            Technician tecnico = technicianRepositoryPort.findById(currentUserId).orElse(null);
            if (tecnico != null) {
                return ticketRepositoryPort.findByTecnicoAsignadoId(tecnico.getId());
            }
            return List.of();
        }
        return ticketRepositoryPort.findBySolicitanteId(currentUserId);
    }

    private Technician findBestTechnician(Ticket ticket, String suggestedTechCode) {
        if (suggestedTechCode != null) {
            Optional<Technician> techOpt = technicianRepositoryPort.findByDocumento(suggestedTechCode);
            if (techOpt.isPresent()) {
                Technician tech = techOpt.get();
                long activas = ticketRepositoryPort.countActiveTicketsByTecnicoId(tech.getId());
                int max = tech.getMaxTicketsActivos() != null ? tech.getMaxTicketsActivos() : 5;
                if (tech.isActivo() && tech.isEspecialidadCompatible(ticket.getCategoria()) && activas < max) {
                    return tech;
                }
            }
        }

        List<Technician> candidatos = technicianRepositoryPort.findByEspecialidad(ticket.getCategoria().name());

        return candidatos.stream()
                .filter(Technician::isActivo)
                .filter(t -> {
                    long activas = ticketRepositoryPort.countActiveTicketsByTecnicoId(t.getId());
                    int max = t.getMaxTicketsActivos() != null ? t.getMaxTicketsActivos() : 5;
                    return activas < max;
                })
                .min(Comparator
                        .comparing((Technician t) -> t.getSede() != null && t.getSede().equalsIgnoreCase(ticket.getSede()) ? 0 : 1)
                        .thenComparingLong(t -> ticketRepositoryPort.countActiveTicketsByTecnicoId(t.getId())))
                .orElse(null);
    }
}
