package com.riwi.support.infrastructure.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.riwi.support.domain.model.Role;
import com.riwi.support.domain.model.User;
import com.riwi.support.domain.model.UserStatus;
import com.riwi.support.domain.port.output.UserRepositoryPort;
import com.riwi.support.infrastructure.metrics.HelpFlowMetrics;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.io.IOException;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SecurityInfrastructureTest {

    @Mock
    private UserRepositoryPort userRepositoryPort;

    @Mock
    private HelpFlowMetrics metrics;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    private ObjectMapper objectMapper;
    private CustomUserDetailsService userDetailsService;
    private JwtAuthenticationEntryPoint entryPoint;
    private CustomAccessDeniedHandler accessDeniedHandler;
    private JwtAuthenticationFilter jwtFilter;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        userDetailsService = new CustomUserDetailsService(userRepositoryPort);
        entryPoint = new JwtAuthenticationEntryPoint(objectMapper, metrics);
        accessDeniedHandler = new CustomAccessDeniedHandler(objectMapper, metrics);
        jwtFilter = new JwtAuthenticationFilter(jwtTokenProvider, userDetailsService);
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("CustomUserDetails: getters y banderas booleanas de seguridad")
    void testCustomUserDetails() {
        CustomUserDetails details = new CustomUserDetails(
                5L, "test@helpflow.com", "pass123", true,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USUARIO"))
        );

        assertEquals(5L, details.id());
        assertEquals("test@helpflow.com", details.getUsername());
        assertEquals("pass123", details.getPassword());
        assertTrue(details.isEnabled());
        assertTrue(details.isAccountNonExpired());
        assertTrue(details.isAccountNonLocked());
        assertTrue(details.isCredentialsNonExpired());
        assertEquals(1, details.getAuthorities().size());
    }

    @Test
    @DisplayName("CustomUserDetailsService: usuario encontrado y usuario inexistente")
    void testCustomUserDetailsService() {
        User user = User.builder()
                .id(1L)
                .correo("admin@helpflow.com")
                .password("encoded_pass")
                .estado(UserStatus.ACTIVO)
                .rol(Role.ROLE_ADMIN)
                .build();

        when(userRepositoryPort.findByCorreo("admin@helpflow.com")).thenReturn(Optional.of(user));

        UserDetails details = userDetailsService.loadUserByUsername("admin@helpflow.com");
        assertNotNull(details);
        assertEquals("admin@helpflow.com", details.getUsername());
        assertTrue(details.isEnabled());

        when(userRepositoryPort.findByCorreo("notfound@helpflow.com")).thenReturn(Optional.empty());
        assertThrows(UsernameNotFoundException.class, () ->
                userDetailsService.loadUserByUsername("notfound@helpflow.com")
        );
    }

    @Test
    @DisplayName("JwtAuthenticationEntryPoint: debe escribir ProblemDetail y status 401")
    void testJwtAuthenticationEntryPoint() throws IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/tickets");
        MockHttpServletResponse response = new MockHttpServletResponse();

        entryPoint.commence(request, response, new BadCredentialsException("No token"));

        assertEquals(401, response.getStatus());
        assertTrue(response.getContentAsString().contains("Fallo de Autenticaci"));
        verify(metrics).incrementAuthFailure();
    }

    @Test
    @DisplayName("CustomAccessDeniedHandler: debe escribir ProblemDetail y status 403")
    void testCustomAccessDeniedHandler() throws IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/technicians");
        MockHttpServletResponse response = new MockHttpServletResponse();

        accessDeniedHandler.handle(request, response, new AccessDeniedException("Forbidden"));

        assertEquals(403, response.getStatus());
        assertTrue(response.getContentAsString().contains("Acceso Prohibido"));
        verify(metrics).incrementAccessDenied();
    }

    @Test
    @DisplayName("JwtAuthenticationFilter: con token válido establece autenticación en SecurityContext")
    void testJwtFilterValidToken() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer valid.jwt.token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = mock(FilterChain.class);

        when(jwtTokenProvider.validateToken("valid.jwt.token")).thenReturn(true);
        when(jwtTokenProvider.getUsernameFromToken("valid.jwt.token")).thenReturn("user@helpflow.com");

        User user = User.builder()
                .id(2L)
                .correo("user@helpflow.com")
                .password("pwd")
                .estado(UserStatus.ACTIVO)
                .rol(Role.ROLE_USUARIO)
                .build();
        when(userRepositoryPort.findByCorreo("user@helpflow.com")).thenReturn(Optional.of(user));

        jwtFilter.doFilter(request, response, filterChain);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth);
        assertEquals("user@helpflow.com", auth.getName());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("JwtAuthenticationFilter: sin header Authorization continúa sin autenticar")
    void testJwtFilterWithoutToken() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = mock(FilterChain.class);

        jwtFilter.doFilter(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("SecurityConfig: verifica beans de passwordEncoder y authenticationProvider")
    void testSecurityConfigBeans() {
        SecurityConfig config = new SecurityConfig(jwtFilter, userDetailsService, entryPoint, accessDeniedHandler);
        PasswordEncoder encoder = config.passwordEncoder();
        assertNotNull(encoder);
        assertTrue(encoder.matches("123456", encoder.encode("123456")));

        AuthenticationProvider authProvider = config.authenticationProvider();
        assertNotNull(authProvider);
    }
}

