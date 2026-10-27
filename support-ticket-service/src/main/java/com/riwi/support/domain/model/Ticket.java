package com.riwi.support.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Ticket {

    private Long id;
    private String titulo;
    private String descripcion;
    private TicketCategory categoria;
    private TicketPriority prioridad;
    private String sede;
    private String activoAfectado;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaLimiteSla;
    private LocalDateTime fechaPrimeraRespuesta;
    private LocalDateTime fechaResolucion;
    private TicketStatus estado;

    private User solicitante;
    private Technician tecnicoAsignado;

    @Builder.Default
    private List<TicketHistory> historiales = new ArrayList<>();

    public boolean isVencido() {
        if (fechaLimiteSla == null) return false;
        if (fechaResolucion != null) {
            return fechaResolucion.isAfter(fechaLimiteSla);
        }
        return LocalDateTime.now().isAfter(fechaLimiteSla);
    }

    public boolean isProximoAVencer(int horasMargen) {
        if (fechaLimiteSla == null || estado == TicketStatus.RESUELTO || estado == TicketStatus.CERRADO || estado == TicketStatus.CANCELADO) {
            return false;
        }
        LocalDateTime ahora = LocalDateTime.now();
        LocalDateTime umbral = ahora.plusHours(horasMargen);
        return ahora.isBefore(fechaLimiteSla) && umbral.isAfter(fechaLimiteSla);
    }
}
