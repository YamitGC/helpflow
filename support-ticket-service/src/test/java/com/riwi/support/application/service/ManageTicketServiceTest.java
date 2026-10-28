package com.riwi.support.application.service;

import com.riwi.support.application.mapper.TicketApplicationMapper;
import com.riwi.support.domain.exception.AccessDeniedDomainException;
import com.riwi.support.domain.exception.BusinessRuleException;
import com.riwi.support.domain.exception.InvalidStateTransitionException;
import com.riwi.support.domain.exception.ResourceNotFoundException;
import com.riwi.support.domain.model.Role;
import com.riwi.support.domain.model.Technician;
import com.riwi.support.domain.model.Ticket;
import com.riwi.support.domain.model.TicketCategory;
import com.riwi.support.domain.model.TicketHistory;
import com.riwi.support.domain.model.TicketPriority;
import com.riwi.support.domain.model.TicketStatus;
import com.riwi.support.domain.model.User;
import com.riwi.support.domain.model.UserStatus;
import com.riwi.support.domain.port.input.ManageTicketUseCase;
import com.riwi.support.domain.port.output.TechnicianDispatchPort;
import com.riwi.support.domain.port.output.TechnicianRepositoryPort;
import com.riwi.support.domain.port.output.TicketRepositoryPort;
import com.riwi.support.domain.port.output.UserRepositoryPort;
import com.riwi.support.infrastructure.metrics.HelpFlowMetrics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ManageTicketServiceTest {

    @Mock
    private TicketRepositoryPort ticketRepositoryPort;

    @Mock
    private UserRepositoryPort userRepositoryPort;

    @Mock
    private TechnicianRepositoryPort technicianRepositoryPort;

    @Mock
    private TechnicianDispatchPort technicianDispatchPort;

    @Mock
    private TicketApplicationMapper ticketMapper;

    @Mock
    private HelpFlowMetrics metrics;

    @InjectMocks
    private ManageTicketService manageTicketService;

    private User solicitanteActivo;
    private User adminActivo;
    private Technician tecnicoHardware;
    private Ticket ticket;

    @BeforeEach
    void setUp() {
        solicitanteActivo = User.builder()
                .id(1L)
                .documento("1001")
                .nombre("Juan Perez")
                .correo("juan@helpflow.com")
                .sede("Medellin")
                .area("Operaciones")
                .rol(Role.ROLE_USUARIO)
                .estado(UserStatus.ACTIVO)
                .build();

        adminActivo = User.builder()
                .id(99L)
                .documento("ADMIN-01")
                .nombre("Admin Carlos")
                .correo("admin@helpflow.com")
                .rol(Role.ROLE_ADMIN)
                .estado(UserStatus.ACTIVO)
                .build();

        tecnicoHardware = Technician.builder()
                .id(10L)
                .documento("TEC-001")
                .nombre("Sofia Tech")
                .correo("sofia@helpflow.com")
                .especialidad("HARDWARE")
                .sede("Medellin")
                .estado(UserStatus.ACTIVO)
                .maxTicketsActivos(5)
                .build();

        ticket = Ticket.builder()
                .id(125L)
                .titulo("Falla de monitor")
                .descripcion("La pantalla no enciende")
                .categoria(TicketCategory.HARDWARE)
                .prioridad(TicketPriority.ALTA)
                .sede("Medellin")
                .estado(TicketStatus.ABIERTO)
                .solicitante(solicitanteActivo)
                .historiales(new ArrayList<>())
                .fechaCreacion(LocalDateTime.now())
                .fechaLimiteSla(LocalDateTime.now().plusHours(8))
                .build();
    }

    @Test
    @DisplayName("Debe crear ticket y autoasignar exitosamente")
    void shouldCreateTicketAndAutoAssignSuccessfully() {
        ManageTicketUseCase.CreateTicketCommand command = new ManageTicketUseCase.CreateTicketCommand(
                1L, "Falla de monitor", "La pantalla no enciende",
                TicketCategory.HARDWARE, TicketPriority.ALTA, "Medellin", "PC-01"
        );

        when(userRepositoryPort.findById(1L)).thenReturn(Optional.of(solicitanteActivo));
        when(ticketMapper.toDomain(eq(command), eq(solicitanteActivo), any(), any())).thenReturn(ticket);
        when(ticketRepositoryPort.save(any(Ticket.class))).thenReturn(ticket);
        when(ticketMapper.toHistory(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(TicketHistory.builder().id(1L).build());

        when(ticketRepositoryPort.findById(125L)).thenReturn(Optional.of(ticket));
        when(technicianDispatchPort.requestTechnician(eq(125L), eq(TicketCategory.HARDWARE), eq(TicketPriority.ALTA)))
                .thenReturn(new TechnicianDispatchPort.DispatchResult(125L, "TEC-001", "ALTA", 2));

        when(technicianRepositoryPort.findByDocumento("TEC-001")).thenReturn(Optional.of(tecnicoHardware));
        when(technicianRepositoryPort.findById(10L)).thenReturn(Optional.of(tecnicoHardware));
        when(ticketRepositoryPort.countActiveTicketsByTecnicoId(10L)).thenReturn(2L);

        Ticket resultado = manageTicketService.createTicket(command);

        assertNotNull(resultado);
        assertEquals(TicketStatus.ASIGNADO, resultado.getEstado());
        assertEquals(tecnicoHardware, resultado.getTecnicoAsignado());
        verify(technicianDispatchPort).requestTechnician(125L, TicketCategory.HARDWARE, TicketPriority.ALTA);
    }

    @Test
    @DisplayName("Debe lanzar excepción si el usuario solicitante está INACTIVO")
    void shouldThrowExceptionWhenUserIsInactive() {
        solicitanteActivo.setEstado(UserStatus.INACTIVO);
        when(userRepositoryPort.findById(1L)).thenReturn(Optional.of(solicitanteActivo));

        ManageTicketUseCase.CreateTicketCommand command = new ManageTicketUseCase.CreateTicketCommand(
                1L, "Falla", "Desc", TicketCategory.HARDWARE, TicketPriority.BAJA, "Medellin", "PC"
        );

        assertThrows(BusinessRuleException.class, () -> manageTicketService.createTicket(command));
    }

    @Test
    @DisplayName("autoAssignTicket no debe reasignar si el ticket no está ABIERTO")
    void shouldNotAutoAssignIfNotAbierto() {
        ticket.setEstado(TicketStatus.EN_PROGRESO);
        when(ticketRepositoryPort.findById(125L)).thenReturn(Optional.of(ticket));

        Ticket result = manageTicketService.autoAssignTicket(125L);

        assertEquals(TicketStatus.EN_PROGRESO, result.getEstado());
        verify(technicianDispatchPort, never()).requestTechnician(any(), any(), any());
    }

    @Test
    @DisplayName("autoAssignTicket retorna ticket original si ningún técnico cumple los criterios")
    void shouldReturnTicketIfNoTechnicianAvailable() {
        ticket.setEstado(TicketStatus.ABIERTO);
        when(ticketRepositoryPort.findById(125L)).thenReturn(Optional.of(ticket));
        when(technicianDispatchPort.requestTechnician(any(), any(), any()))
                .thenReturn(new TechnicianDispatchPort.DispatchResult(125L, "TEC-UNKNOWN", "BAJA", 10));
        when(technicianRepositoryPort.findByDocumento("TEC-UNKNOWN")).thenReturn(Optional.empty());
        when(technicianRepositoryPort.findByEspecialidad("HARDWARE")).thenReturn(List.of());

        Ticket result = manageTicketService.autoAssignTicket(125L);

        assertEquals(TicketStatus.ABIERTO, result.getEstado());
    }

    @Test
    @DisplayName("autoAssignTicket selecciona mejor técnico por especialidad y sede cuando no hay sugerencia directa")
    void shouldSelectBestTechnicianBySpecialtyAndSede() {
        ticket.setEstado(TicketStatus.ABIERTO);
        when(ticketRepositoryPort.findById(125L)).thenReturn(Optional.of(ticket));
        when(technicianDispatchPort.requestTechnician(any(), any(), any()))
                .thenReturn(new TechnicianDispatchPort.DispatchResult(125L, null, "MEDIA", 4));

        Technician techBogota = Technician.builder().id(20L).estado(UserStatus.ACTIVO).especialidad("HARDWARE").sede("Bogota").maxTicketsActivos(5).build();
        when(technicianRepositoryPort.findByEspecialidad("HARDWARE")).thenReturn(List.of(techBogota, tecnicoHardware));
        when(ticketRepositoryPort.countActiveTicketsByTecnicoId(20L)).thenReturn(1L);
        when(ticketRepositoryPort.countActiveTicketsByTecnicoId(10L)).thenReturn(1L);

        when(technicianRepositoryPort.findById(10L)).thenReturn(Optional.of(tecnicoHardware));
        when(ticketMapper.toHistory(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(TicketHistory.builder().build());
        when(ticketRepositoryPort.save(any())).thenReturn(ticket);

        Ticket result = manageTicketService.autoAssignTicket(125L);

        assertEquals(TicketStatus.ASIGNADO, result.getEstado());
    }

    @Test
    @DisplayName("assignTicket debe lanzar excepción si el técnico está INACTIVO")
    void shouldThrowExceptionWhenTechnicianIsInactive() {
        tecnicoHardware.setEstado(UserStatus.INACTIVO);
        when(ticketRepositoryPort.findById(125L)).thenReturn(Optional.of(ticket));
        when(technicianRepositoryPort.findById(10L)).thenReturn(Optional.of(tecnicoHardware));

        assertThrows(BusinessRuleException.class, () ->
                manageTicketService.assignTicket(125L, 10L, 99L, "Asignación"));
    }

    @Test
    @DisplayName("assignTicket debe lanzar excepción si la especialidad del técnico es incompatible")
    void shouldThrowExceptionWhenTechnicianSpecialtyIsIncompatible() {
        tecnicoHardware.setEspecialidad("SOFTWARE");
        when(ticketRepositoryPort.findById(125L)).thenReturn(Optional.of(ticket));
        when(technicianRepositoryPort.findById(10L)).thenReturn(Optional.of(tecnicoHardware));

        assertThrows(BusinessRuleException.class, () ->
                manageTicketService.assignTicket(125L, 10L, 99L, "Asignación"));
    }

    @Test
    @DisplayName("assignTicket debe lanzar excepción si el técnico alcanzó su carga máxima")
    void shouldThrowExceptionWhenTechnicianReachesMaxCapacity() {
        when(ticketRepositoryPort.findById(125L)).thenReturn(Optional.of(ticket));
        when(technicianRepositoryPort.findById(10L)).thenReturn(Optional.of(tecnicoHardware));
        when(ticketRepositoryPort.countActiveTicketsByTecnicoId(10L)).thenReturn(5L);

        assertThrows(BusinessRuleException.class, () ->
                manageTicketService.assignTicket(125L, 10L, 99L, "Asignación"));
    }

    @Test
    @DisplayName("assignTicket asigna correctamente con usuario administrador")
    void shouldAssignTicketWithAdminUser() {
        when(ticketRepositoryPort.findById(125L)).thenReturn(Optional.of(ticket));
        when(technicianRepositoryPort.findById(10L)).thenReturn(Optional.of(tecnicoHardware));
        when(ticketRepositoryPort.countActiveTicketsByTecnicoId(10L)).thenReturn(1L);
        when(userRepositoryPort.findById(99L)).thenReturn(Optional.of(adminActivo));
        when(ticketMapper.toHistory(any(), eq(99L), eq("Admin Carlos"), any(), any(), any(), any()))
                .thenReturn(TicketHistory.builder().build());
        when(ticketRepositoryPort.save(any())).thenReturn(ticket);

        Ticket assigned = manageTicketService.assignTicket(125L, 10L, 99L, "Asignado por Admin");

        assertEquals(TicketStatus.ASIGNADO, assigned.getEstado());
        assertEquals(tecnicoHardware, assigned.getTecnicoAsignado());
    }

    @Test
    @DisplayName("updateTicketStatus pasa a EN_PROGRESO y registra métrica de primera respuesta")
    void shouldUpdateStatusToInProgressAndRecordMetric() {
        ticket.setEstado(TicketStatus.ASIGNADO);
        when(ticketRepositoryPort.findById(125L)).thenReturn(Optional.of(ticket));
        when(userRepositoryPort.findById(1L)).thenReturn(Optional.of(solicitanteActivo));
        when(ticketMapper.toHistory(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(TicketHistory.builder().build());
        when(ticketRepositoryPort.save(any())).thenReturn(ticket);

        Ticket updated = manageTicketService.updateTicketStatus(125L, TicketStatus.EN_PROGRESO, "Iniciando diagnóstico", 1L);

        assertEquals(TicketStatus.EN_PROGRESO, updated.getEstado());
        assertNotNull(updated.getFechaPrimeraRespuesta());
        verify(metrics).recordFirstResponseTime(any());
    }

    @Test
    @DisplayName("updateTicketStatus pasa a RESUELTO y registra métrica de resolución y vencimiento")
    void shouldUpdateStatusToResolvedAndRecordResolutionTime() {
        ticket.setEstado(TicketStatus.EN_PROGRESO);
        ticket.setFechaLimiteSla(LocalDateTime.now().minusHours(1)); // Vencido
        when(ticketRepositoryPort.findById(125L)).thenReturn(Optional.of(ticket));
        when(userRepositoryPort.findById(1L)).thenReturn(Optional.of(solicitanteActivo));
        when(ticketMapper.toHistory(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(TicketHistory.builder().build());
        when(ticketRepositoryPort.save(any())).thenReturn(ticket);

        Ticket updated = manageTicketService.updateTicketStatus(125L, TicketStatus.RESUELTO, "Monitor reparado", 1L);

        assertEquals(TicketStatus.RESUELTO, updated.getEstado());
        assertNotNull(updated.getFechaResolucion());
        verify(metrics).recordResolutionTime(any());
        verify(metrics).incrementExpiredTickets();
    }

    @Test
    @DisplayName("Debe lanzar InvalidStateTransitionException si la transición de estado es inválida")
    void shouldThrowExceptionOnInvalidStateTransition() {
        ticket.setEstado(TicketStatus.ABIERTO);
        when(ticketRepositoryPort.findById(125L)).thenReturn(Optional.of(ticket));
        when(userRepositoryPort.findById(1L)).thenReturn(Optional.of(solicitanteActivo));

        assertThrows(InvalidStateTransitionException.class, () ->
                manageTicketService.updateTicketStatus(125L, TicketStatus.RESUELTO, "Resolviendo sin asignar", 1L)
        );
    }

    @Test
    @DisplayName("getTicketById: solicitante o admin pueden consultar, otro usuario lanza AccessDeniedDomainException")
    void shouldTestGetTicketByIdPermissions() {
        when(ticketRepositoryPort.findById(125L)).thenReturn(Optional.of(ticket));

        // Solicitante accede
        Ticket t1 = manageTicketService.getTicketById(125L, 1L, false);
        assertEquals(125L, t1.getId());

        // Admin accede aunque no sea el solicitante
        Ticket t2 = manageTicketService.getTicketById(125L, 999L, true);
        assertEquals(125L, t2.getId());

        // Tercero sin rol es bloqueado
        assertThrows(AccessDeniedDomainException.class, () ->
                manageTicketService.getTicketById(125L, 888L, false));
        verify(metrics).incrementAccessDenied();
    }

    @Test
    @DisplayName("getTicketsForCurrentUser: Admin, Técnico y Usuario estándar")
    void shouldGetTicketsForCurrentUserByRole() {
        // Admin
        when(ticketRepositoryPort.findAll()).thenReturn(List.of(ticket));
        List<Ticket> adminList = manageTicketService.getTicketsForCurrentUser(99L, true, false);
        assertEquals(1, adminList.size());

        // Técnico con registro
        when(technicianRepositoryPort.findById(10L)).thenReturn(Optional.of(tecnicoHardware));
        when(ticketRepositoryPort.findByTecnicoAsignadoId(10L)).thenReturn(List.of(ticket));
        List<Ticket> techList = manageTicketService.getTicketsForCurrentUser(10L, false, true);
        assertEquals(1, techList.size());

        // Técnico sin registro
        when(technicianRepositoryPort.findById(11L)).thenReturn(Optional.empty());
        List<Ticket> emptyTechList = manageTicketService.getTicketsForCurrentUser(11L, false, true);
        assertTrue(emptyTechList.isEmpty());

        // Usuario estándar
        when(ticketRepositoryPort.findBySolicitanteId(1L)).thenReturn(List.of(ticket));
        List<Ticket> userList = manageTicketService.getTicketsForCurrentUser(1L, false, false);
        assertEquals(1, userList.size());
    }

    @Test
    @DisplayName("Debe escalar automáticamente tickets según prioridad actual")
    void shouldEscalateTicketsNearingSlaBreach() {
        ticket.setPrioridad(TicketPriority.BAJA);
        Ticket ticketMedia = Ticket.builder().id(2L).prioridad(TicketPriority.MEDIA).estado(TicketStatus.ABIERTO).historiales(new ArrayList<>()).build();
        Ticket ticketAlta = Ticket.builder().id(3L).prioridad(TicketPriority.ALTA).estado(TicketStatus.ASIGNADO).historiales(new ArrayList<>()).build();
        Ticket ticketCritica = Ticket.builder().id(4L).prioridad(TicketPriority.CRITICA).estado(TicketStatus.EN_PROGRESO).historiales(new ArrayList<>()).build();

        when(ticketRepositoryPort.findTicketsNearingSlaBreach()).thenReturn(List.of(ticket, ticketMedia, ticketAlta, ticketCritica));
        when(ticketMapper.toHistory(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(TicketHistory.builder().id(2L).build());

        manageTicketService.checkAndEscalateTicketsNearingSlaBreach();

        assertEquals(TicketPriority.MEDIA, ticket.getPrioridad());
        assertEquals(TicketPriority.ALTA, ticketMedia.getPrioridad());
        assertEquals(TicketPriority.CRITICA, ticketAlta.getPrioridad());
        assertEquals(TicketPriority.CRITICA, ticketCritica.getPrioridad());
    }
}

