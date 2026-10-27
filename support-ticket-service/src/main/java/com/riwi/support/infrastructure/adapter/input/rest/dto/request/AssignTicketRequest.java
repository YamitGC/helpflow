package com.riwi.support.infrastructure.adapter.input.rest.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssignTicketRequest {

    @NotNull(message = "El ID del técnico es obligatorio")
    private Long tecnicoId;

    private String motivo;
}

