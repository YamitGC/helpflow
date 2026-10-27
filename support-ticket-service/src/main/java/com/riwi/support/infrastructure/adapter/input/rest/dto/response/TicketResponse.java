package com.riwi.support.infrastructure.adapter.input.rest.dto.response;

import com.riwi.support.domain.model.TicketCategory;
import com.riwi.support.domain.model.TicketPriority;
import com.riwi.support.domain.model.TicketStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketResponse {

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
    private boolean vencido;

    private Long solicitanteId;
    private String solicitanteNombre;
    private String solicitanteCorreo;

    private Long tecnicoId;
    private String tecnicoNombre;
    private String tecnicoEspecialidad;

    private List<HistoryResponse> historiales;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HistoryResponse {
        private Long id;
        private Long usuarioAccionId;
        private String usuarioAccionNombre;
        private String estadoAnterior;
        private String estadoNuevo;
        private String observaciones;
        private LocalDateTime fechaCambio;
    }
}

