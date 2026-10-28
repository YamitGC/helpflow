package com.riwi.support.infrastructure.adapter.output.persistence.adapter;

import com.riwi.support.domain.model.Role;
import com.riwi.support.domain.model.User;
import com.riwi.support.domain.model.UserStatus;
import com.riwi.support.infrastructure.adapter.output.persistence.entity.UserEntity;
import com.riwi.support.infrastructure.adapter.output.persistence.mapper.UserPersistenceMapper;
import com.riwi.support.infrastructure.adapter.output.persistence.repository.JpaUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserRepositoryAdapterTest {

    @Mock
    private JpaUserRepository jpaUserRepository;

    @Mock
    private UserPersistenceMapper userMapper;

    @InjectMocks
    private UserRepositoryAdapter userRepositoryAdapter;

    private User userDomain;
    private UserEntity userEntity;

    @BeforeEach
    void setUp() {
        userDomain = User.builder().id(1L).correo("adapter@helpflow.com").documento("999").rol(Role.ROLE_USUARIO).build();
        userEntity = UserEntity.builder().id(1L).correo("adapter@helpflow.com").documento("999").rol(Role.ROLE_USUARIO).build();
    }

    @Test
    @DisplayName("Debe guardar usuario usando el mapper y el repositorio JPA")
    void shouldSaveUser() {
        when(userMapper.toEntity(userDomain)).thenReturn(userEntity);
        when(jpaUserRepository.save(userEntity)).thenReturn(userEntity);
        when(userMapper.toDomain(userEntity)).thenReturn(userDomain);

        User saved = userRepositoryAdapter.save(userDomain);

        assertNotNull(saved);
        assertEquals("adapter@helpflow.com", saved.getCorreo());
        verify(jpaUserRepository).save(userEntity);
    }

    @Test
    @DisplayName("Debe buscar usuario por ID, correo y documento")
    void shouldFindUser() {
        when(jpaUserRepository.findById(1L)).thenReturn(Optional.of(userEntity));
        when(userMapper.toDomain(userEntity)).thenReturn(userDomain);
        when(jpaUserRepository.findByCorreo("adapter@helpflow.com")).thenReturn(Optional.of(userEntity));
        when(jpaUserRepository.findByDocumento("999")).thenReturn(Optional.of(userEntity));
        when(jpaUserRepository.existsByCorreo("adapter@helpflow.com")).thenReturn(true);
        when(jpaUserRepository.existsByDocumento("999")).thenReturn(true);

        assertTrue(userRepositoryAdapter.findById(1L).isPresent());
        assertTrue(userRepositoryAdapter.findByCorreo("adapter@helpflow.com").isPresent());
        assertTrue(userRepositoryAdapter.findByDocumento("999").isPresent());
        assertTrue(userRepositoryAdapter.existsByCorreo("adapter@helpflow.com"));
        assertTrue(userRepositoryAdapter.existsByDocumento("999"));
    }

    @Test
    @DisplayName("Debe listar todos los usuarios y filtrar por rol")
    void shouldFindAllAndFindByRol() {
        when(jpaUserRepository.findAll()).thenReturn(List.of(userEntity));
        when(userMapper.toDomain(userEntity)).thenReturn(userDomain);
        when(jpaUserRepository.findByRol(Role.ROLE_USUARIO)).thenReturn(List.of(userEntity));

        List<User> all = userRepositoryAdapter.findAll();
        assertEquals(1, all.size());

        List<User> byRole = userRepositoryAdapter.findByRol(Role.ROLE_USUARIO);
        assertEquals(1, byRole.size());
    }
}
