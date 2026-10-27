package com.riwi.support.domain.port.input;

import com.riwi.support.domain.model.Ticket;
import com.riwi.support.domain.model.TicketCategory;
import com.riwi.support.domain.model.TicketPriority;
import com.riwi.support.domain.model.TicketStatus;

import java.util.List;

public interface ManageTicketUseCase {

    Ticket createTicket(CreateTicketCommand command);

    Ticket assignTicket(Long ticketId, Long tecnicoId, Long adminOrSystemUserId, String motivo);

    Ticket autoAssignTicket(Long ticketId);

    Ticket updateTicketStatus(Long ticketId, TicketStatus nuevoEstado, String observaciones, Long usuarioAccionId);

    void checkAndEscalateTicketsNearingSlaBreach();

    Ticket getTicketById(Long ticketId, Long currentUserId, boolean isAdminOrTech);

    List<Ticket> getTicketsForCurrentUser(Long currentUserId, boolean isAdmin, boolean isTecnico);

    record CreateTicketCommand(
            Long solicitanteId,
            String titulo,
            String descripcion,
            TicketCategory categoria,
            TicketPriority prioridad,
            String sede,
            String activoAfectado
    ) {}
}

