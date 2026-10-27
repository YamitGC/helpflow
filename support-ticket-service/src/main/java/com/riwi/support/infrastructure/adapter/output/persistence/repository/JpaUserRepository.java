package com.riwi.support.infrastructure.adapter.output.persistence.repository;

import com.riwi.support.domain.model.Role;
import com.riwi.support.infrastructure.adapter.output.persistence.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JpaUserRepository extends JpaRepository<UserEntity, Long> {

    Optional<UserEntity> findByCorreo(String correo);

    Optional<UserEntity> findByDocumento(String documento);

    boolean existsByCorreo(String correo);

    boolean existsByDocumento(String documento);

    List<UserEntity> findByRol(Role rol);
}

