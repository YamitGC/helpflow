package com.riwi.support.domain.model;

public enum TicketPriority {
    BAJA(48),     // Horas de SLA por defecto según prioridad
    MEDIA(24),
    ALTA(8),
    CRITICA(2);

    private final int slaHours;

    TicketPriority(int slaHours) {
        this.slaHours = slaHours;
    }

    public int getSlaHours() {
        return slaHours;
    }
}

