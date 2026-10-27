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
public class User {

    private Long id;
    private String documento;
    private String nombre;
    private String correo;
    private String password;
    private String sede;
    private String area;
    private Role rol;
    private UserStatus estado;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public boolean isActivo() {
        return UserStatus.ACTIVO.equals(this.estado);
    }
}
