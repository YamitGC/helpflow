package com.riwi.support.infrastructure.adapter.input.rest.dto.request;

import com.riwi.support.domain.model.TicketCategory;
import com.riwi.support.domain.model.TicketPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateTicketRequest {

    @NotBlank(message = "El título del ticket es obligatorio")
    @Size(min = 5, max = 200, message = "El título debe tener entre 5 y 200 caracteres")
    private String titulo;

    @NotBlank(message = "La descripción del ticket es obligatoria")
    private String descripcion;

    @NotNull(message = "La categoría es obligatoria (HARDWARE, SOFTWARE, RED, ACCESOS, OTRO)")
    private TicketCategory categoria;

    @NotNull(message = "La prioridad es obligatoria (BAJA, MEDIA, ALTA, CRITICA)")
    private TicketPriority prioridad;

    private String sede;

    private String activoAfectado;
}

