package com.riwi.support.infrastructure.adapter.input.rest.handler;

import com.riwi.support.domain.exception.AccessDeniedDomainException;
import com.riwi.support.domain.exception.BusinessRuleException;
import com.riwi.support.domain.exception.InvalidStateTransitionException;
import com.riwi.support.domain.exception.ResourceNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/api/test");
    }

    @Test
    @DisplayName("handleNotFound debe retornar HTTP 404 y ProblemDetail RFC 7807")
    void shouldHandleResourceNotFoundException() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Recurso no hallado");
        ProblemDetail problem = handler.handleNotFound(ex, request);

        assertEquals(HttpStatus.NOT_FOUND.value(), problem.getStatus());
        assertEquals("Recurso No Encontrado", problem.getTitle());
        assertEquals("Recurso no hallado", problem.getDetail());
        assertNotNull(problem.getProperties().get("traceId"));
        assertNotNull(problem.getProperties().get("timestamp"));
        assertEquals("/api/test", problem.getInstance().toString());
    }

    @Test
    @DisplayName("handleBusinessRule debe retornar HTTP 422 para BusinessRuleException e InvalidStateTransitionException")
    void shouldHandleBusinessRuleAndInvalidTransition() {
        BusinessRuleException ex1 = new BusinessRuleException("Regla rota");
        ProblemDetail p1 = handler.handleBusinessRule(ex1, request);
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY.value(), p1.getStatus());
        assertEquals("Regla de Negocio Incumplida", p1.getTitle());

        InvalidStateTransitionException ex2 = new InvalidStateTransitionException("Transición inválida");
        ProblemDetail p2 = handler.handleBusinessRule(ex2, request);
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY.value(), p2.getStatus());
    }

    @Test
    @DisplayName("handleAccessDenied debe retornar HTTP 403 para AccessDeniedDomainException y AccessDeniedException")
    void shouldHandleAccessDeniedExceptions() {
        AccessDeniedDomainException ex1 = new AccessDeniedDomainException("Sin permiso");
        ProblemDetail p1 = handler.handleAccessDenied(ex1, request);
        assertEquals(HttpStatus.FORBIDDEN.value(), p1.getStatus());
        assertEquals("Acceso Denegado", p1.getTitle());

        AccessDeniedException ex2 = new AccessDeniedException("Forbidden");
        ProblemDetail p2 = handler.handleAccessDenied(ex2, request);
        assertEquals(HttpStatus.FORBIDDEN.value(), p2.getStatus());
    }

    @Test
    @DisplayName("handleBadCredentials debe retornar HTTP 401")
    void shouldHandleBadCredentialsException() {
        BadCredentialsException ex = new BadCredentialsException("Bad creds");
        ProblemDetail problem = handler.handleBadCredentials(ex, request);

        assertEquals(HttpStatus.UNAUTHORIZED.value(), problem.getStatus());
        assertEquals("Error de Autenticación", problem.getTitle());
        assertEquals("Credenciales incorrectas", problem.getDetail());
    }

    @Test
    @DisplayName("handleValidationErrors debe retornar HTTP 400 y el mapa de campos inválidos")
    void shouldHandleValidationErrors() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("object", "email", "Formato de correo inválido");

        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

        ProblemDetail problem = handler.handleValidationErrors(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST.value(), problem.getStatus());
        assertEquals("Error de Validación", problem.getTitle());
        @SuppressWarnings("unchecked")
        Map<String, String> invalidFields = (Map<String, String>) problem.getProperties().get("invalidFields");
        assertNotNull(invalidFields);
        assertEquals("Formato de correo inválido", invalidFields.get("email"));
    }

    @Test
    @DisplayName("handleGeneralError debe retornar HTTP 500 para excepciones no controladas")
    void shouldHandleGeneralError() {
        RuntimeException ex = new RuntimeException("Error fatal no esperado");
        ProblemDetail problem = handler.handleGeneralError(ex, request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), problem.getStatus());
        assertEquals("Error Interno del Servidor", problem.getTitle());
        assertEquals("Ha ocurrido un error interno inesperado.", problem.getDetail());
    }
}

