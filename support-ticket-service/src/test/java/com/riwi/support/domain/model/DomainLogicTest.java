package com.riwi.support.domain.model;

import com.riwi.support.domain.exception.AccessDeniedDomainException;
import com.riwi.support.domain.exception.BusinessRuleException;
import com.riwi.support.domain.exception.InvalidStateTransitionException;
import com.riwi.support.domain.exception.ResourceNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

        // Ticket vencido sin resolución
        Ticket ticketPasado = Ticket.builder()
                .fechaLimiteSla(ahora.minusHours(1))
                .estado(TicketStatus.ASIGNADO)
                .build();
        assertTrue(ticketPasado.isVencido());

        // Ticket resuelto a tiempo vs fuera de tiempo
        Ticket ticketResueltoATiempo = Ticket.builder()
                .fechaLimiteSla(ahora.plusHours(2))
                .fechaResolucion(ahora.plusHours(1))
                .estado(TicketStatus.RESUELTO)
                .build();
        assertFalse(ticketResueltoATiempo.isVencido());
        assertFalse(ticketResueltoATiempo.isProximoAVencer(5)); // Resuelto no aplica proximo a vencer

        Ticket ticketResueltoTarde = Ticket.builder()
                .fechaLimiteSla(ahora.minusHours(2))
                .fechaResolucion(ahora.minusHours(1))
                .estado(TicketStatus.RESUELTO)
                .build();
        assertTrue(ticketResueltoTarde.isVencido());

        // Ticket cerrado o cancelado no debe ser proximo a vencer
        Ticket ticketCerrado = Ticket.builder().fechaLimiteSla(ahora.plusHours(1)).estado(TicketStatus.CERRADO).build();
        assertFalse(ticketCerrado.isProximoAVencer(5));

        Ticket ticketCancelado = Ticket.builder().fechaLimiteSla(ahora.plusHours(1)).estado(TicketStatus.CANCELADO).build();
        assertFalse(ticketCancelado.isProximoAVencer(5));

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

        // Especialidad nula
        tech.setEspecialidad(null);
        assertFalse(tech.isEspecialidadCompatible(TicketCategory.RED));

        User user = User.builder()
                .estado(UserStatus.ACTIVO)
                .build();
        assertTrue(user.isActivo());

        user.setEstado(UserStatus.INACTIVO);
        assertFalse(user.isActivo());
    }

    @Test
    @DisplayName("Valida constructores de excepciones de dominio")
    void testDomainExceptions() {
        AccessDeniedDomainException ex1 = new AccessDeniedDomainException("Acceso denegado");
        assertEquals("Acceso denegado", ex1.getMessage());

        ResourceNotFoundException ex2 = new ResourceNotFoundException("No encontrado");
        assertEquals("No encontrado", ex2.getMessage());

        BusinessRuleException ex3 = new BusinessRuleException("Regla");
        assertEquals("Regla", ex3.getMessage());

        InvalidStateTransitionException ex4 = new InvalidStateTransitionException("Transición");
        assertEquals("Transición", ex4.getMessage());
    }
}

