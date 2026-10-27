package com.riwi.dispatch.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DispatchResponse {

    private Long ticketId;
    private String tecnicoId;
    private String disponibilidad;
    private Integer tiempoEstimadoHoras;
}

