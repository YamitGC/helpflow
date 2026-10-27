package com.riwi.support.infrastructure.adapter.input.rest.dto.request;

import com.riwi.support.domain.model.Role;
import jakarta.validation.constraints.Email;
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
public class RegisterRequest {

    @NotBlank(message = "El documento es obligatorio")
    @Size(min = 4, max = 50, message = "El documento debe tener entre 4 y 50 caracteres")
    private String documento;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 3, max = 150, message = "El nombre debe tener entre 3 y 150 caracteres")
    private String nombre;

    @NotBlank(message = "El correo corporativo es obligatorio")
    @Email(message = "El formato de correo corporativo no es válido")
    private String correo;

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
    private String password;

    @NotBlank(message = "La sede es obligatoria")
    private String sede;

    @NotBlank(message = "El área es obligatoria")
    private String area;

    @NotNull(message = "El rol es obligatorio")
    private Role rol;
}
