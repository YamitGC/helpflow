package com.riwi.support.domain.model;

import java.util.List;

public enum TicketStatus {
    ABIERTO,
    ASIGNADO,
    EN_PROGRESO,
    RESUELTO,
    CERRADO,
    CANCELADO;

    /**
     * Reglas del enunciado:
     * Transiciones de estado válidas y trazables.
     */
    public boolean canTransitionTo(TicketStatus target) {
        if (this == target) return true;
        return switch (this) {
            case ABIERTO -> List.of(ASIGNADO, CANCELADO).contains(target);
            case ASIGNADO -> List.of(EN_PROGRESO, CANCELADO).contains(target);
            case EN_PROGRESO -> List.of(RESUELTO, CANCELADO).contains(target);
            case RESUELTO -> List.of(CERRADO, EN_PROGRESO).contains(target);
            case CERRADO, CANCELADO -> false; // Estados finales
        };
    }
}

