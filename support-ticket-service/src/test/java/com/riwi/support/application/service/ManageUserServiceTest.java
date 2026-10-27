package com.riwi.support.application.service;

import com.riwi.support.application.mapper.UserApplicationMapper;
import com.riwi.support.domain.exception.BusinessRuleException;
import com.riwi.support.domain.exception.ResourceNotFoundException;
import com.riwi.support.domain.model.Role;
import com.riwi.support.domain.model.User;
import com.riwi.support.domain.model.UserStatus;
import com.riwi.support.domain.port.input.ManageUserUseCase;
import com.riwi.support.domain.port.output.UserRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ManageUserServiceTest {

    @Mock
    private UserRepositoryPort userRepositoryPort;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserApplicationMapper userMapper;

    @InjectMocks
    private ManageUserService manageUserService;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .documento("12345")
                .nombre("Test User")
                .correo("test@helpflow.com")
                .password("encoded_pass")
                .sede("Medellin")
                .area("TI")
                .rol(Role.ROLE_USUARIO)
                .estado(UserStatus.ACTIVO)
                .build();
    }

    @Test
    @DisplayName("Debe registrar un usuario exitosamente")
    void shouldRegisterUserSuccessfully() {
        ManageUserUseCase.RegisterUserCommand command = new ManageUserUseCase.RegisterUserCommand(
                "12345", "Test User", "test@helpflow.com", "plain_pass", "Medellin", "TI", Role.ROLE_USUARIO
        );

        when(userRepositoryPort.existsByDocumento("12345")).thenReturn(false);
        when(userRepositoryPort.existsByCorreo("test@helpflow.com")).thenReturn(false);
        when(userMapper.toDomain(command)).thenReturn(user);
        when(passwordEncoder.encode("plain_pass")).thenReturn("encoded_pass");
        when(userRepositoryPort.save(any(User.class))).thenReturn(user);

        User saved = manageUserService.registerUser(command);

        assertNotNull(saved);
        assertEquals("test@helpflow.com", saved.getCorreo());
        verify(userRepositoryPort).save(user);
    }

    @Test
    @DisplayName("Debe fallar al registrar usuario si el documento ya existe")
    void shouldThrowExceptionWhenDocumentExists() {
        ManageUserUseCase.RegisterUserCommand command = new ManageUserUseCase.RegisterUserCommand(
                "12345", "Test", "test@helpflow.com", "pass", "Sede", "Area", Role.ROLE_USUARIO
        );
        when(userRepositoryPort.existsByDocumento("12345")).thenReturn(true);

        assertThrows(BusinessRuleException.class, () -> manageUserService.registerUser(command));
    }

    @Test
    @DisplayName("Debe actualizar perfil de usuario")
    void shouldUpdateUserProfile() {
        ManageUserUseCase.UpdateProfileCommand command = new ManageUserUseCase.UpdateProfileCommand("Nuevo Nombre", "Bogota", "Ventas");
        when(userRepositoryPort.findById(1L)).thenReturn(Optional.of(user));
        when(userRepositoryPort.save(user)).thenReturn(user);

        User updated = manageUserService.updateProfile(1L, command);

        assertNotNull(updated);
        verify(userMapper).updateDomainFromCommand(command, user);
        verify(userRepositoryPort).save(user);
    }

    @Test
    @DisplayName("Debe obtener usuario por ID y lanzar excepción si no existe")
    void shouldFindByIdOrThrow() {
        when(userRepositoryPort.findById(1L)).thenReturn(Optional.of(user));
        when(userRepositoryPort.findById(99L)).thenReturn(Optional.empty());

        assertEquals("Test User", manageUserService.getById(1L).getNombre());
        assertThrows(ResourceNotFoundException.class, () -> manageUserService.getById(99L));
    }

    @Test
    @DisplayName("Debe listar todos los usuarios")
    void shouldListAllUsers() {
        when(userRepositoryPort.findAll()).thenReturn(List.of(user));
        assertEquals(1, manageUserService.getAllUsers().size());
    }
}

