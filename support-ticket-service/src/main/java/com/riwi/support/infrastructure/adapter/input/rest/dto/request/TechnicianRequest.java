package com.riwi.support.infrastructure.adapter.input.rest.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TechnicianRequest {

    @NotBlank(message = "El documento es obligatorio")
    private String documento;

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "Formato de correo inválido")
    private String correo;

    @NotNull(message = "El salario es obligatorio")
    @DecimalMin(value = "0.0", inclusive = false, message = "El salario debe ser mayor a 0")
    private BigDecimal salario;

    @NotNull(message = "La fecha de afiliación es obligatoria")
    private LocalDate fechaAfiliacion;

    @NotBlank(message = "La especialidad es obligatoria (HARDWARE, SOFTWARE, RED, ACCESOS, OTRO)")
    private String especialidad;

    @NotBlank(message = "La sede es obligatoria")
    private String sede;

    private Integer maxTicketsActivos;
}

