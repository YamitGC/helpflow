package com.riwi.support.domain.port.input;

import com.riwi.support.domain.model.Role;
import com.riwi.support.domain.model.User;

import java.util.List;

public interface ManageUserUseCase {

    User registerUser(RegisterUserCommand command);

    User updateProfile(Long userId, UpdateProfileCommand command);

    User getById(Long id);

    User getByCorreo(String correo);

    List<User> getAllUsers();

    record RegisterUserCommand(
            String documento,
            String nombre,
            String correo,
            String password,
            String sede,
            String area,
            Role rol
    ) {}

    record UpdateProfileCommand(
            String nombre,
            String sede,
            String area
    ) {}
}
