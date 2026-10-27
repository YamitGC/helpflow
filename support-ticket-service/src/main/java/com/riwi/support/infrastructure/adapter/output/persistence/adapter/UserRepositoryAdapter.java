package com.riwi.support.infrastructure.adapter.output.persistence.adapter;

import com.riwi.support.domain.model.Role;
import com.riwi.support.domain.model.User;
import com.riwi.support.domain.port.output.UserRepositoryPort;
import com.riwi.support.infrastructure.adapter.output.persistence.entity.UserEntity;
import com.riwi.support.infrastructure.adapter.output.persistence.mapper.UserPersistenceMapper;
import com.riwi.support.infrastructure.adapter.output.persistence.repository.JpaUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UserRepositoryAdapter implements UserRepositoryPort {

    private final JpaUserRepository jpaUserRepository;
    private final UserPersistenceMapper userMapper;

    @Override
    public User save(User user) {
        UserEntity entity = userMapper.toEntity(user);
        UserEntity saved = jpaUserRepository.save(entity);
        return userMapper.toDomain(saved);
    }

    @Override
    public Optional<User> findById(Long id) {
        return jpaUserRepository.findById(id).map(userMapper::toDomain);
    }

    @Override
    public Optional<User> findByCorreo(String correo) {
        return jpaUserRepository.findByCorreo(correo).map(userMapper::toDomain);
    }

    @Override
    public Optional<User> findByDocumento(String documento) {
        return jpaUserRepository.findByDocumento(documento).map(userMapper::toDomain);
    }

    @Override
    public boolean existsByCorreo(String correo) {
        return jpaUserRepository.existsByCorreo(correo);
    }

    @Override
    public boolean existsByDocumento(String documento) {
        return jpaUserRepository.existsByDocumento(documento);
    }

    @Override
    public List<User> findByRol(Role rol) {
        return jpaUserRepository.findByRol(rol).stream()
                .map(userMapper::toDomain)
                .toList();
    }

    @Override
    public List<User> findAll() {
        return jpaUserRepository.findAll().stream()
                .map(userMapper::toDomain)
                .toList();
    }
}

