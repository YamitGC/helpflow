package com.riwi.support.infrastructure.adapter.input.rest.dto.response;

import com.riwi.support.domain.model.UserStatus;
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
public class TechnicianResponse {

    private Long id;
    private String documento;
    private String nombre;
    private String correo;
    private BigDecimal salario;
    private LocalDate fechaAfiliacion;
    private String especialidad;
    private String sede;
    private UserStatus estado;
    private Integer maxTicketsActivos;
}

