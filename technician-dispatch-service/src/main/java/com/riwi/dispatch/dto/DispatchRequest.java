package com.riwi.dispatch.dto;

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
public class DispatchRequest {

    @NotNull(message = "El ticketId es obligatorio")
    private Long ticketId;

    @NotBlank(message = "La categoria es obligatoria")
    private String categoria;

    @NotBlank(message = "La prioridad es obligatoria")
    private String prioridad;
}

