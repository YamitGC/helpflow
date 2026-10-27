package com.riwi.support.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DomainLogicTest {

    @Test
    @DisplayName("Valida transiciones válidas e inválidas de la máquina de estados de TicketStatus")
    void testTicketStatusTransitions() {
        // Transiciones válidas
        assertTrue(TicketStatus.ABIERTO.canTransitionTo(TicketStatus.ASIGNADO));
        assertTrue(TicketStatus.ABIERTO.canTransitionTo(TicketStatus.CANCELADO));
        assertTrue(TicketStatus.ASIGNADO.canTransitionTo(TicketStatus.EN_PROGRESO));
        assertTrue(TicketStatus.EN_PROGRESO.canTransitionTo(TicketStatus.RESUELTO));
        assertTrue(TicketStatus.RESUELTO.canTransitionTo(TicketStatus.CERRADO));
        assertTrue(TicketStatus.RESUELTO.canTransitionTo(TicketStatus.EN_PROGRESO));

        // Mismo estado siempre es válido
        assertTrue(TicketStatus.ABIERTO.canTransitionTo(TicketStatus.ABIERTO));

        // Transiciones inválidas
        assertFalse(TicketStatus.ABIERTO.canTransitionTo(TicketStatus.RESUELTO));
        assertFalse(TicketStatus.CERRADO.canTransitionTo(TicketStatus.ABIERTO));
        assertFalse(TicketStatus.CANCELADO.canTransitionTo(TicketStatus.EN_PROGRESO));
    }

    @Test
    @DisplayName("Valida cálculo de vencimiento e SLA en modelo Ticket")
    void testTicketSlaLogic() {
        LocalDateTime ahora = LocalDateTime.now();

        // Ticket no vencido
        Ticket ticketFuturo = Ticket.builder()
                .fechaLimiteSla(ahora.plusHours(4))
                .estado(TicketStatus.ASIGNADO)
                .build();
        assertFalse(ticketFuturo.isVencido());
        assertTrue(ticketFuturo.isProximoAVencer(5));
        assertFalse(ticketFuturo.isProximoAVencer(2));

        // Ticket vencido
        Ticket ticketPasado = Ticket.builder()
                .fechaLimiteSla(ahora.minusHours(1))
                .estado(TicketStatus.ASIGNADO)
                .build();
        assertTrue(ticketPasado.isVencido());

        // Ticket sin fecha de SLA
        Ticket ticketSinSla = Ticket.builder().build();
        assertFalse(ticketSinSla.isVencido());
        assertFalse(ticketSinSla.isProximoAVencer(2));
    }

    @Test
    @DisplayName("Valida compatibilidad y estado activo en modelo Technician y User")
    void testTechnicianAndUserMethods() {
        Technician tech = Technician.builder()
                .estado(UserStatus.ACTIVO)
                .especialidad("RED")
                .build();

        assertTrue(tech.isActivo());
        assertTrue(tech.isEspecialidadCompatible(TicketCategory.RED));
        assertFalse(tech.isEspecialidadCompatible(TicketCategory.HARDWARE));

        User user = User.builder()
                .estado(UserStatus.ACTIVO)
                .build();
        assertTrue(user.isActivo());

        user.setEstado(UserStatus.INACTIVO);
        assertFalse(user.isActivo());
    }
}

