package com.riwi.support.infrastructure.adapter.input.rest.dto.request;

import com.riwi.support.domain.model.TicketStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateStatusRequest {

    @NotNull(message = "El nuevo estado es obligatorio")
    private TicketStatus nuevoEstado;

    @NotBlank(message = "Las observaciones del cambio de estado son obligatorias para trazabilidad")
    private String observaciones;
}

