package com.riwi.support.infrastructure.adapter.input.rest.handler;

import com.riwi.support.domain.exception.AccessDeniedDomainException;
import com.riwi.support.domain.exception.BusinessRuleException;
import com.riwi.support.domain.exception.InvalidStateTransitionException;
import com.riwi.support.domain.exception.ResourceNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Manejador Global de Errores con formato RFC 7807 (ProblemDetail) según enunciado:
 * - type
 * - title
 * - status
 * - detail
 * - instance
 * - timestamp
 * - traceId
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ProblemDetail handleNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        log.warn("Recurso no encontrado: {}", ex.getMessage());
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setTitle("Recurso No Encontrado");
        problem.setType(URI.create("https://helpflow.com/errors/not-found"));
        enrichProblemDetail(problem, request);
        return problem;
    }

    @ExceptionHandler({BusinessRuleException.class, InvalidStateTransitionException.class})
    public ProblemDetail handleBusinessRule(RuntimeException ex, HttpServletRequest request) {
        log.warn("Violación de regla de negocio: {}", ex.getMessage());
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
        problem.setTitle("Regla de Negocio Incumplida");
        problem.setType(URI.create("https://helpflow.com/errors/business-rule"));
        enrichProblemDetail(problem, request);
        return problem;
    }

    @ExceptionHandler({AccessDeniedDomainException.class, AccessDeniedException.class})
    public ProblemDetail handleAccessDenied(Exception ex, HttpServletRequest request) {
        log.warn("Acceso denegado: {}", ex.getMessage());
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, ex.getMessage());
        problem.setTitle("Acceso Denegado");
        problem.setType(URI.create("https://helpflow.com/errors/forbidden"));
        enrichProblemDetail(problem, request);
        return problem;
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ProblemDetail handleBadCredentials(BadCredentialsException ex, HttpServletRequest request) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, "Credenciales incorrectas");
        problem.setTitle("Error de Autenticación");
        problem.setType(URI.create("https://helpflow.com/errors/unauthorized"));
        enrichProblemDetail(problem, request);
        return problem;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidationErrors(MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, String> errors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.put(error.getField(), error.getDefaultMessage());
        }

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Los datos enviados contienen errores de validación");
        problem.setTitle("Error de Validación");
        problem.setType(URI.create("https://helpflow.com/errors/validation-error"));
        problem.setProperty("invalidFields", errors);
        enrichProblemDetail(problem, request);
        return problem;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGeneralError(Exception ex, HttpServletRequest request) {
        log.error("Error no controlado en el servidor: ", ex);
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "Ha ocurrido un error interno inesperado.");
        problem.setTitle("Error Interno del Servidor");
        problem.setType(URI.create("https://helpflow.com/errors/internal-server-error"));
        enrichProblemDetail(problem, request);
        return problem;
    }

    private void enrichProblemDetail(ProblemDetail problem, HttpServletRequest request) {
        problem.setInstance(URI.create(request.getRequestURI()));
        problem.setProperty("timestamp", Instant.now());
        problem.setProperty("traceId", UUID.randomUUID().toString());
    }
}

