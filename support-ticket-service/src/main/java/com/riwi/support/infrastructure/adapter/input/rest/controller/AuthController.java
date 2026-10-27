package com.riwi.support.infrastructure.adapter.input.rest.controller;

import com.riwi.support.domain.model.User;
import com.riwi.support.domain.port.input.ManageUserUseCase;
import com.riwi.support.infrastructure.adapter.input.rest.dto.request.LoginRequest;
import com.riwi.support.infrastructure.adapter.input.rest.dto.request.RegisterRequest;
import com.riwi.support.infrastructure.adapter.input.rest.dto.response.AuthResponse;
import com.riwi.support.infrastructure.adapter.input.rest.dto.response.UserResponse;
import com.riwi.support.infrastructure.adapter.input.rest.mapper.RestDtoMapper;
import com.riwi.support.infrastructure.security.JwtTokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticación", description = "Registro e inicio de sesión con JWT")
public class AuthController {

    private final ManageUserUseCase manageUserUseCase;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final RestDtoMapper mapper;

    @PostMapping("/register")
    @Operation(summary = "Registrar un nuevo usuario en el sistema")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        User registered = manageUserUseCase.registerUser(mapper.toCommand(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(registered));
    }

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión y obtener token JWT")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getCorreo(), request.getPassword())
        );

        User user = manageUserUseCase.getByCorreo(request.getCorreo());
        String token = tokenProvider.generateToken(user);

        AuthResponse response = AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .userId(user.getId())
                .nombre(user.getNombre())
                .correo(user.getCorreo())
                .rol(user.getRol().name())
                .build();

        return ResponseEntity.ok(response);
    }
}

