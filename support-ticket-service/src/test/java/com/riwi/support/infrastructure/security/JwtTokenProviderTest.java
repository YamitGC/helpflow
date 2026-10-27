package com.riwi.support.infrastructure.security;

import com.riwi.support.domain.model.Role;
import com.riwi.support.domain.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtTokenProviderTest {

    private JwtTokenProvider tokenProvider;
    private User testUser;

    @BeforeEach
    void setUp() {
        // Llave de 256 bits para HMAC-SHA256
        String secret = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";
        long expirationMillis = 3600000; // 1 hora
        tokenProvider = new JwtTokenProvider(secret, expirationMillis);

        testUser = User.builder()
                .id(42L)
                .nombre("Security User")
                .correo("security@helpflow.com")
                .rol(Role.ROLE_ADMIN)
                .build();
    }

    @Test
    @DisplayName("Debe generar token JWT válido y extraer claims correctamente")
    void testGenerateAndParseToken() {
        String token = tokenProvider.generateToken(testUser);

        assertNotNull(token);
        assertTrue(tokenProvider.validateToken(token));
        assertEquals("security@helpflow.com", tokenProvider.getUsernameFromToken(token));
        assertEquals(42L, tokenProvider.getUserIdFromToken(token));
        assertEquals("ROLE_ADMIN", tokenProvider.getRoleFromToken(token));
    }

    @Test
    @DisplayName("Debe retornar false para tokens inválidos")
    void testInvalidToken() {
        assertFalse(tokenProvider.validateToken("token.invalido.falso"));
        assertFalse(tokenProvider.validateToken(""));
    }
}

