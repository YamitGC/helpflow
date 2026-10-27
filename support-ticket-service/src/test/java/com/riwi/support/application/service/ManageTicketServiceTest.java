package com.riwi.support.application.service;

import com.riwi.support.application.mapper.TicketApplicationMapper;
import com.riwi.support.domain.exception.BusinessRuleException;
import com.riwi.support.domain.exception.InvalidStateTransitionException;
import com.riwi.support.domain.model.Role;
import com.riwi.support.domain.model.Technician;
import com.riwi.support.domain.model.Ticket;
import com.riwi.support.domain.model.TicketCategory;
import com.riwi.support.domain.model.TicketHistory;
import com.riwi.support.domain.model.TicketPriority;
import com.riwi.support.domain.model.TicketStatus;
import com.riwi.support.domain.model.User;
import com.riwi.support.domain.model.UserStatus;
import com.riwi.support.domain.port.input.ManageTicketUseCase;
import com.riwi.support.domain.port.output.TechnicianDispatchPort;
import com.riwi.support.domain.port.output.TechnicianRepositoryPort;
import com.riwi.support.domain.port.output.TicketRepositoryPort;
import com.riwi.support.domain.port.output.UserRepositoryPort;
import com.riwi.support.infrastructure.metrics.HelpFlowMetrics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ManageTicketServiceTest {

    @Mock
    private TicketRepositoryPort ticketRepositoryPort;

    @Mock
    private UserRepositoryPort userRepositoryPort;

    @Mock
    private TechnicianRepositoryPort technicianRepositoryPort;

    @Mock
    private TechnicianDispatchPort technicianDispatchPort;

    @Mock
    private TicketApplicationMapper ticketMapper;

    @Mock
    private HelpFlowMetrics metrics;

    @InjectMocks
    private ManageTicketService manageTicketService;

    private User solicitanteActivo;
    private Technician tecnicoHardware;
    private Ticket ticket;

    @BeforeEach
    void setUp() {
        solicitanteActivo = User.builder()
                .id(1L)
                .documento("1001")
                .nombre("Juan Perez")
                .correo("juan@helpflow.com")
                .sede("Medellin")
                .area("Operaciones")
                .rol(Role.ROLE_USUARIO)
                .estado(UserStatus.ACTIVO)
                .build();

        tecnicoHardware = Technician.builder()
                .id(10L)
                .documento("TEC-001")
                .nombre("Sofia Tech")
                .correo("sofia@helpflow.com")
                .especialidad("HARDWARE")
                .sede("Medellin")
                .estado(UserStatus.ACTIVO)
                .maxTicketsActivos(5)
                .build();

        ticket = Ticket.builder()
                .id(125L)
                .titulo("Falla de monitor")
                .descripcion("La pantalla no enciende")
                .categoria(TicketCategory.HARDWARE)
                .prioridad(TicketPriority.ALTA)
                .sede("Medellin")
                .estado(TicketStatus.ABIERTO)
                .solicitante(solicitanteActivo)
                .historiales(new ArrayList<>())
                .fechaCreacion(LocalDateTime.now())
                .fechaLimiteSla(LocalDateTime.now().plusHours(8))
                .build();
    }

    @Test
    @DisplayName("Debe crear ticket y autoasignar exitosamente usando el mock de TechnicianDispatchPort")
    void shouldCreateTicketAndAutoAssignSuccessfully() {
        ManageTicketUseCase.CreateTicketCommand command = new ManageTicketUseCase.CreateTicketCommand(
                1L, "Falla de monitor", "La pantalla no enciende",
                TicketCategory.HARDWARE, TicketPriority.ALTA, "Medellin", "PC-01"
        );

        when(userRepositoryPort.findById(1L)).thenReturn(Optional.of(solicitanteActivo));
        when(ticketMapper.toDomain(eq(command), eq(solicitanteActivo), any(), any())).thenReturn(ticket);
        when(ticketRepositoryPort.save(any(Ticket.class))).thenReturn(ticket);
        when(ticketMapper.toHistory(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(TicketHistory.builder().id(1L).build());

        when(ticketRepositoryPort.findById(125L)).thenReturn(Optional.of(ticket));
        when(technicianDispatchPort.requestTechnician(eq(125L), eq(TicketCategory.HARDWARE), eq(TicketPriority.ALTA)))
                .thenReturn(new TechnicianDispatchPort.DispatchResult(125L, "TEC-001", "ALTA", 2));

        when(technicianRepositoryPort.findByDocumento("TEC-001")).thenReturn(Optional.of(tecnicoHardware));
        when(technicianRepositoryPort.findById(10L)).thenReturn(Optional.of(tecnicoHardware));
        when(ticketRepositoryPort.countActiveTicketsByTecnicoId(10L)).thenReturn(2L);

        Ticket resultado = manageTicketService.createTicket(command);

        assertNotNull(resultado);
        assertEquals(TicketStatus.ASIGNADO, resultado.getEstado());
        assertEquals(tecnicoHardware, resultado.getTecnicoAsignado());
        verify(technicianDispatchPort).requestTechnician(125L, TicketCategory.HARDWARE, TicketPriority.ALTA);
    }

    @Test
    @DisplayName("Debe lanzar excepción si el usuario solicitante está INACTIVO")
    void shouldThrowExceptionWhenUserIsInactive() {
        solicitanteActivo.setEstado(UserStatus.INACTIVO);
        when(userRepositoryPort.findById(1L)).thenReturn(Optional.of(solicitanteActivo));

        ManageTicketUseCase.CreateTicketCommand command = new ManageTicketUseCase.CreateTicketCommand(
                1L, "Falla", "Desc", TicketCategory.HARDWARE, TicketPriority.BAJA, "Medellin", "PC"
        );

        assertThrows(BusinessRuleException.class, () -> manageTicketService.createTicket(command));
    }

    @Test
    @DisplayName("Debe lanzar InvalidStateTransitionException si la transición de estado es inválida")
    void shouldThrowExceptionOnInvalidStateTransition() {
        ticket.setEstado(TicketStatus.ABIERTO);
        when(ticketRepositoryPort.findById(125L)).thenReturn(Optional.of(ticket));
        when(userRepositoryPort.findById(1L)).thenReturn(Optional.of(solicitanteActivo));

        // ABIERTO no puede pasar directo a RESUELTO (debe pasar por ASIGNADO / EN_PROGRESO)
        assertThrows(InvalidStateTransitionException.class, () ->
                manageTicketService.updateTicketStatus(125L, TicketStatus.RESUELTO, "Resolviendo sin asignar", 1L)
        );
    }

    @Test
    @DisplayName("Debe escalar automáticamente tickets próximos a vencer según SLA")
    void shouldEscalateTicketsNearingSlaBreach() {
        ticket.setPrioridad(TicketPriority.BAJA);
        when(ticketRepositoryPort.findTicketsNearingSlaBreach()).thenReturn(List.of(ticket));
        when(ticketMapper.toHistory(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(TicketHistory.builder().id(2L).build());

        manageTicketService.checkAndEscalateTicketsNearingSlaBreach();

        // Si era BAJA debe escalar a MEDIA
        assertEquals(TicketPriority.MEDIA, ticket.getPrioridad());
        verify(ticketRepositoryPort).save(ticket);
    }
}

