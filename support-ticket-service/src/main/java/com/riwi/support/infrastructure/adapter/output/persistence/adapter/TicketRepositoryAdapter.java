package com.riwi.support.infrastructure.adapter.output.persistence.adapter;

import com.riwi.support.domain.model.Ticket;
import com.riwi.support.domain.model.TicketCategory;
import com.riwi.support.domain.model.TicketPriority;
import com.riwi.support.domain.port.output.TicketRepositoryPort;
import com.riwi.support.infrastructure.adapter.output.persistence.entity.TicketEntity;
import com.riwi.support.infrastructure.adapter.output.persistence.mapper.TicketPersistenceMapper;
import com.riwi.support.infrastructure.adapter.output.persistence.repository.JpaTicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class TicketRepositoryAdapter implements TicketRepositoryPort {

    private final JpaTicketRepository jpaTicketRepository;
    private final TicketPersistenceMapper ticketMapper;

    @Override
    public Ticket save(Ticket ticket) {
        TicketEntity entity = ticketMapper.toEntity(ticket);
        TicketEntity saved = jpaTicketRepository.save(entity);
        return ticketMapper.toDomain(saved);
    }

    @Override
    public Optional<Ticket> findById(Long id) {
        return jpaTicketRepository.findWithDetailsById(id).map(ticketMapper::toDomain);
    }

    @Override
    public List<Ticket> findBySolicitanteId(Long solicitanteId) {
        return jpaTicketRepository.findBySolicitanteId(solicitanteId).stream()
                .map(ticketMapper::toDomain)
                .toList();
    }

    @Override
    public List<Ticket> findByTecnicoAsignadoId(Long tecnicoId) {
        return jpaTicketRepository.findByTecnicoAsignadoId(tecnicoId).stream()
                .map(ticketMapper::toDomain)
                .toList();
    }

    @Override
    public List<Ticket> findAll() {
        return jpaTicketRepository.findAll().stream()
                .map(ticketMapper::toDomain)
                .toList();
    }

    @Override
    public long countActiveTicketsByTecnicoId(Long tecnicoId) {
        return jpaTicketRepository.countActiveTicketsByTecnicoId(tecnicoId);
    }

    @Override
    public long countByCategoria(TicketCategory categoria) {
        return jpaTicketRepository.countByCategoria(categoria);
    }

    @Override
    public long countByPrioridad(TicketPriority prioridad) {
        return jpaTicketRepository.countByPrioridad(prioridad);
    }

    @Override
    public List<Ticket> findTicketsNearingSlaBreach() {
        // Tickets cuyo SLA vence dentro de las próximas 2 horas
        LocalDateTime umbral = LocalDateTime.now().plusHours(2);
        return jpaTicketRepository.findTicketsNearingSlaBreach(umbral).stream()
                .map(ticketMapper::toDomain)
                .toList();
    }
}

