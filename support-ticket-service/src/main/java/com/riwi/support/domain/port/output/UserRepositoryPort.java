package com.riwi.support.domain.port.output;

import com.riwi.support.domain.model.Role;
import com.riwi.support.domain.model.User;

import java.util.List;
import java.util.Optional;

public interface UserRepositoryPort {

    User save(User user);

    Optional<User> findById(Long id);

    Optional<User> findByCorreo(String correo);

    Optional<User> findByDocumento(String documento);

    boolean existsByCorreo(String correo);

    boolean existsByDocumento(String documento);

    List<User> findByRol(Role rol);

    List<User> findAll();
}

