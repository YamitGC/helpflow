package com.riwi.support.domain.port.output;

import com.riwi.support.domain.model.Ticket;
import com.riwi.support.domain.model.TicketCategory;
import com.riwi.support.domain.model.TicketPriority;
import com.riwi.support.domain.model.TicketStatus;

import java.util.List;
import java.util.Optional;

public interface TicketRepositoryPort {

    Ticket save(Ticket ticket);

    Optional<Ticket> findById(Long id);

    List<Ticket> findBySolicitanteId(Long solicitanteId);

    List<Ticket> findByTecnicoAsignadoId(Long tecnicoId);

    List<Ticket> findAll();

    long countActiveTicketsByTecnicoId(Long tecnicoId);

    long countByCategoria(TicketCategory categoria);

    long countByPrioridad(TicketPriority prioridad);

    List<Ticket> findTicketsNearingSlaBreach();
}

