package com.riwi.support.infrastructure.adapter.input.rest.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.riwi.support.domain.model.Role;
import com.riwi.support.domain.model.TicketCategory;
import com.riwi.support.domain.model.TicketPriority;
import com.riwi.support.domain.model.TicketStatus;
import com.riwi.support.domain.model.User;
import com.riwi.support.domain.model.UserStatus;
import com.riwi.support.domain.port.output.UserRepositoryPort;
import com.riwi.support.infrastructure.adapter.input.rest.dto.request.CreateTicketRequest;
import com.riwi.support.infrastructure.adapter.input.rest.dto.request.LoginRequest;
import com.riwi.support.infrastructure.adapter.input.rest.dto.request.RegisterRequest;
import com.riwi.support.infrastructure.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthAndTicketIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserRepositoryPort userRepositoryPort;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private User usuarioPrueba;
    private String jwtToken;

    @BeforeEach
    void setup() {
        usuarioPrueba = User.builder()
                .id(1L)
                .documento("1001")
                .nombre("Usuario Test")
                .correo("test@helpflow.com")
                .password(passwordEncoder.encode("password123"))
                .sede("Medellin")
                .area("TI")
                .rol(Role.ROLE_USUARIO)
                .estado(UserStatus.ACTIVO)
                .build();

        jwtToken = jwtTokenProvider.generateToken(usuarioPrueba);
    }

    @Test
    @DisplayName("Debe autenticarse y retornar token JWT válido")
    void shouldLoginAndReturnJwtToken() throws Exception {
        when(userRepositoryPort.findByCorreo("test@helpflow.com")).thenReturn(Optional.of(usuarioPrueba));

        LoginRequest loginRequest = LoginRequest.builder()
                .correo("test@helpflow.com")
                .password("password123")
                .build();

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.correo").value("test@helpflow.com"));
    }

    @Test
    @DisplayName("Debe rechazar petición no autenticada con ProblemDetail (RFC 7807)")
    void shouldRejectUnauthenticatedRequestWithProblemDetail() throws Exception {
        mockMvc.perform(get("/tickets"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.title").value("Fallo de Autenticación"))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.detail").isNotEmpty())
                .andExpect(jsonPath("$.timestamp").isNotEmpty());
    }

    @Test
    @DisplayName("Debe validar Bean Validation en registro de usuario")
    void shouldValidateRegistrationRequest() throws Exception {
        RegisterRequest invalidRequest = RegisterRequest.builder()
                .correo("correo-invalido") // Correo con formato malo
                .build();

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Error de Validación"))
                .andExpect(jsonPath("$.invalidFields").isMap());
    }
}

