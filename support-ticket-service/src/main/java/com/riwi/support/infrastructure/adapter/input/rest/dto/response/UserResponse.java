package com.riwi.support.infrastructure.adapter.input.rest.dto.response;

import com.riwi.support.domain.model.Role;
import com.riwi.support.domain.model.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private Long id;
    private String documento;
    private String nombre;
    private String correo;
    private String sede;
    private String area;
    private Role rol;
    private UserStatus estado;
}

