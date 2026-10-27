package com.riwi.support.infrastructure.adapter.input.rest.controller;

import com.riwi.support.domain.model.Ticket;
import com.riwi.support.domain.port.input.ManageTicketUseCase;
import com.riwi.support.infrastructure.adapter.input.rest.dto.request.AssignTicketRequest;
import com.riwi.support.infrastructure.adapter.input.rest.dto.request.CreateTicketRequest;
import com.riwi.support.infrastructure.adapter.input.rest.dto.request.UpdateStatusRequest;
import com.riwi.support.infrastructure.adapter.input.rest.dto.response.PageResponse;
import com.riwi.support.infrastructure.adapter.input.rest.dto.response.TicketResponse;
import com.riwi.support.infrastructure.adapter.input.rest.mapper.RestDtoMapper;
import com.riwi.support.infrastructure.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/tickets")
@RequiredArgsConstructor
@Tag(name = "Tickets", description = "Operaciones del ciclo de vida de los tickets de soporte TI")
@SecurityRequirement(name = "Bearer Authentication")
public class TicketController {

    private final ManageTicketUseCase manageTicketUseCase;
    private final RestDtoMapper mapper;

    @PostMapping
    @PreAuthorize("hasAnyRole('USUARIO', 'ADMIN')")
    @Operation(summary = "Crear un nuevo ticket (Inicia en ABIERTO y ejecuta autoasignación)")
    public ResponseEntity<TicketResponse> createTicket(
            @Valid @RequestBody CreateTicketRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Ticket created = manageTicketUseCase.createTicket(mapper.toCommand(request, userDetails.id()));
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(created));
    }

    @GetMapping
    @Operation(summary = "Listar tickets de forma paginada según el rol y pertenencia")
    public ResponseEntity<PageResponse<TicketResponse>> getTickets(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        boolean isAdmin = userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        boolean isTecnico = userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_TECNICO"));

        List<TicketResponse> tickets = manageTicketUseCase.getTicketsForCurrentUser(userDetails.id(), isAdmin, isTecnico)
                .stream()
                .map(mapper::toResponse)
                .toList();

        return ResponseEntity.ok(PageResponse.of(tickets, page, size));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener el detalle e historial de un ticket por ID")
    public ResponseEntity<TicketResponse> getTicketById(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        boolean isAdminOrTech = userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_TECNICO"));

        Ticket ticket = manageTicketUseCase.getTicketById(id, userDetails.id(), isAdminOrTech);
        return ResponseEntity.ok(mapper.toResponse(ticket));
    }

    @PostMapping("/{id}/assign")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Asignar o reasignar manualmente un ticket a un técnico (Solo ADMIN)")
    public ResponseEntity<TicketResponse> assignTicket(
            @PathVariable Long id,
            @Valid @RequestBody AssignTicketRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Ticket assigned = manageTicketUseCase.assignTicket(id, request.getTecnicoId(), userDetails.id(), request.getMotivo());
        return ResponseEntity.ok(mapper.toResponse(assigned));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'TECNICO', 'USUARIO')")
    @Operation(summary = "Actualizar estado del ticket con observaciones obligatorias")
    public ResponseEntity<TicketResponse> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateStatusRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Ticket updated = manageTicketUseCase.updateTicketStatus(
                id,
                request.getNuevoEstado(),
                request.getObservaciones(),
                userDetails.id()
        );
        return ResponseEntity.ok(mapper.toResponse(updated));
    }

    @PostMapping("/escalate-check")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Disparar revisión y escalamiento automático por SLA de tickets en riesgo")
    public ResponseEntity<String> triggerEscalation() {
        manageTicketUseCase.checkAndEscalateTicketsNearingSlaBreach();
        return ResponseEntity.ok("Revisión de SLA y escalamiento de tickets ejecutado con éxito.");
    }
}

