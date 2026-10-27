package com.riwi.support.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketHistory {

    private Long id;
    private Long ticketId;
    private Long usuarioAccionId;
    private String usuarioAccionNombre;
    private TicketStatus estadoAnterior;
    private TicketStatus estadoNuevo;
    private String observaciones;
    private LocalDateTime fechaCambio;
}

