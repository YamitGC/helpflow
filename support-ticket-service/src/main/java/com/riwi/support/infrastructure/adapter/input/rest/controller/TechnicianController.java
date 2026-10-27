package com.riwi.support.infrastructure.adapter.input.rest.controller;

import com.riwi.support.domain.model.Technician;
import com.riwi.support.domain.port.input.ManageTechnicianUseCase;
import com.riwi.support.infrastructure.adapter.input.rest.dto.request.TechnicianRequest;
import com.riwi.support.infrastructure.adapter.input.rest.dto.response.PageResponse;
import com.riwi.support.infrastructure.adapter.input.rest.dto.response.TechnicianResponse;
import com.riwi.support.infrastructure.adapter.input.rest.mapper.RestDtoMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/technicians")
@RequiredArgsConstructor
@Tag(name = "Técnicos", description = "Gestión de técnicos de soporte (Solo ADMIN)")
@SecurityRequirement(name = "Bearer Authentication")
public class TechnicianController {

    private final ManageTechnicianUseCase manageTechnicianUseCase;
    private final RestDtoMapper mapper;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Registrar un nuevo técnico (Solo ADMIN)")
    public ResponseEntity<TechnicianResponse> registerTechnician(@Valid @RequestBody TechnicianRequest request) {
        Technician registered = manageTechnicianUseCase.registerTechnician(mapper.toCommand(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(registered));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TECNICO')")
    @Operation(summary = "Listar técnicos de forma paginada")
    public ResponseEntity<PageResponse<TechnicianResponse>> getAllTechnicians(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        List<TechnicianResponse> all = manageTechnicianUseCase.getAll().stream()
                .map(mapper::toResponse)
                .toList();

        return ResponseEntity.ok(PageResponse.of(all, page, size));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TECNICO')")
    @Operation(summary = "Obtener detalle de un técnico por ID")
    public ResponseEntity<TechnicianResponse> getTechnicianById(@PathVariable Long id) {
        Technician technician = manageTechnicianUseCase.getById(id);
        return ResponseEntity.ok(mapper.toResponse(technician));
    }
}

