package com.riwi.support.domain.port.output;

import com.riwi.support.domain.model.TicketCategory;
import com.riwi.support.domain.model.TicketPriority;

public interface TechnicianDispatchPort {

    record DispatchResult(
            Long ticketId,
            String tecnicoId,
            String disponibilidad,
            Integer tiempoEstimadoHoras
    ) {}

    DispatchResult requestTechnician(Long ticketId, TicketCategory categoria, TicketPriority prioridad);
}

