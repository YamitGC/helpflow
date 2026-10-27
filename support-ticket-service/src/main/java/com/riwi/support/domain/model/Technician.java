package com.riwi.support.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Modelo de Dominio Técnico.
 * Según el enunciado:
 * - documento, nombre, salario, fecha de afiliación, estado (ACTIVO / INACTIVO)
 * - especialidad (categoría en la que es apto)
 * - sede asignada
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Technician {

    private Long id;
    private String documento;
    private String nombre;
    private String correo;
    private BigDecimal salario;
    private LocalDate fechaAfiliacion;
    private UserStatus estado;
    private String especialidad; // Coincide con TicketCategory (HARDWARE, RED, etc.)
    private String sede;
    private Integer maxTicketsActivos; // Carga máxima de tickets permitida (ej: 5)

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public boolean isActivo() {
        return UserStatus.ACTIVO.equals(this.estado);
    }

    public boolean isEspecialidadCompatible(TicketCategory categoria) {
        if (this.especialidad == null) return false;
        return this.especialidad.equalsIgnoreCase(categoria.name());
    }
}

