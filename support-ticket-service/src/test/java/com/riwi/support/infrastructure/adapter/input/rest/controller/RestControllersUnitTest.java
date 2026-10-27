package com.riwi.support.infrastructure.adapter.input.rest.controller;

import com.riwi.support.domain.model.Role;
import com.riwi.support.domain.model.Technician;
import com.riwi.support.domain.model.Ticket;
import com.riwi.support.domain.model.TicketCategory;
import com.riwi.support.domain.model.TicketPriority;
import com.riwi.support.domain.model.TicketStatus;
import com.riwi.support.domain.model.User;
import com.riwi.support.domain.port.input.ManageTechnicianUseCase;
import com.riwi.support.domain.port.input.ManageTicketUseCase;
import com.riwi.support.domain.port.input.ManageUserUseCase;
import com.riwi.support.infrastructure.adapter.input.rest.dto.request.AssignTicketRequest;
import com.riwi.support.infrastructure.adapter.input.rest.dto.request.CreateTicketRequest;
import com.riwi.support.infrastructure.adapter.input.rest.dto.request.LoginRequest;
import com.riwi.support.infrastructure.adapter.input.rest.dto.request.RegisterRequest;
import com.riwi.support.infrastructure.adapter.input.rest.dto.request.TechnicianRequest;
import com.riwi.support.infrastructure.adapter.input.rest.dto.request.UpdateStatusRequest;
import com.riwi.support.infrastructure.adapter.input.rest.dto.response.AuthResponse;
import com.riwi.support.infrastructure.adapter.input.rest.dto.response.PageResponse;
import com.riwi.support.infrastructure.adapter.input.rest.dto.response.TechnicianResponse;
import com.riwi.support.infrastructure.adapter.input.rest.dto.response.TicketResponse;
import com.riwi.support.infrastructure.adapter.input.rest.dto.response.UserResponse;
import com.riwi.support.infrastructure.adapter.input.rest.mapper.RestDtoMapper;
import com.riwi.support.infrastructure.security.CustomUserDetails;
import com.riwi.support.infrastructure.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RestControllersUnitTest {

    @Mock
    private ManageUserUseCase manageUserUseCase;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtTokenProvider tokenProvider;
    @Mock
    private RestDtoMapper mapper;

    @InjectMocks
    private AuthController authController;

    @Mock
    private ManageTechnicianUseCase manageTechnicianUseCase;
    @InjectMocks
    private TechnicianController technicianController;

    @Mock
    private ManageTicketUseCase manageTicketUseCase;
    @InjectMocks
    private TicketController ticketController;

    private CustomUserDetails userDetails;

    @BeforeEach
    void setUp() {
        userDetails = new CustomUserDetails(
                1L, "user@helpflow.com", "pass", true,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
    }

    @Test
    @DisplayName("AuthController: register y login")
    void testAuthController() {
        RegisterRequest registerReq = RegisterRequest.builder()
                .documento("111").nombre("U").correo("u@h.com").password("123456").sede("S").area("A").rol(Role.ROLE_USUARIO).build();
        User user = User.builder().id(1L).correo("u@h.com").rol(Role.ROLE_USUARIO).build();
        UserResponse userResp = UserResponse.builder().id(1L).correo("u@h.com").build();

        when(mapper.toCommand(registerReq)).thenReturn(new ManageUserUseCase.RegisterUserCommand("111", "U", "u@h.com", "123456", "S", "A", Role.ROLE_USUARIO));
        when(manageUserUseCase.registerUser(any())).thenReturn(user);
        when(mapper.toResponse(user)).thenReturn(userResp);

        ResponseEntity<UserResponse> regEntity = authController.register(registerReq);
        assertEquals(HttpStatus.CREATED, regEntity.getStatusCode());

        // Login
        LoginRequest loginReq = new LoginRequest("u@h.com", "123456");
        when(manageUserUseCase.getByCorreo("u@h.com")).thenReturn(user);
        when(tokenProvider.generateToken(user)).thenReturn("mock.jwt.token");

        ResponseEntity<AuthResponse> logEntity = authController.login(loginReq);
        assertEquals(HttpStatus.OK, logEntity.getStatusCode());
        assertEquals("mock.jwt.token", logEntity.getBody().getToken());
    }

    @Test
    @DisplayName("TechnicianController: register y list")
    void testTechnicianController() {
        TechnicianRequest req = new TechnicianRequest("TEC-1", "Tech", "t@h.com", BigDecimal.TEN, LocalDate.now(), "RED", "Med", 5);
        Technician tech = Technician.builder().id(1L).documento("TEC-1").build();
        TechnicianResponse resp = TechnicianResponse.builder().id(1L).documento("TEC-1").build();

        when(manageTechnicianUseCase.registerTechnician(any())).thenReturn(tech);
        when(mapper.toResponse(tech)).thenReturn(resp);

        ResponseEntity<TechnicianResponse> created = technicianController.registerTechnician(req);
        assertEquals(HttpStatus.CREATED, created.getStatusCode());

        when(manageTechnicianUseCase.getAll()).thenReturn(List.of(tech));
        ResponseEntity<PageResponse<TechnicianResponse>> paged = technicianController.getAllTechnicians(0, 10);
        assertEquals(1, paged.getBody().getTotalElements());
    }

    @Test
    @DisplayName("TicketController: create, assign, status y escalate")
    void testTicketController() {
        CreateTicketRequest createReq = new CreateTicketRequest("Titulo", "Desc", TicketCategory.RED, TicketPriority.ALTA, "Sede", "Activo");
        Ticket ticket = Ticket.builder().id(10L).build();
        TicketResponse ticketResp = TicketResponse.builder().id(10L).build();

        when(manageTicketUseCase.createTicket(any())).thenReturn(ticket);
        when(mapper.toResponse(ticket)).thenReturn(ticketResp);

        ResponseEntity<TicketResponse> createRes = ticketController.createTicket(createReq, userDetails);
        assertEquals(HttpStatus.CREATED, createRes.getStatusCode());

        // Assign
        AssignTicketRequest assignReq = new AssignTicketRequest(2L, "Motivo");
        when(manageTicketUseCase.assignTicket(10L, 2L, 1L, "Motivo")).thenReturn(ticket);
        ResponseEntity<TicketResponse> assignRes = ticketController.assignTicket(10L, assignReq, userDetails);
        assertEquals(HttpStatus.OK, assignRes.getStatusCode());

        // Status
        UpdateStatusRequest statusReq = new UpdateStatusRequest(TicketStatus.EN_PROGRESO, "Obs");
        when(manageTicketUseCase.updateTicketStatus(10L, TicketStatus.EN_PROGRESO, "Obs", 1L)).thenReturn(ticket);
        ResponseEntity<TicketResponse> statusRes = ticketController.updateStatus(10L, statusReq, userDetails);
        assertEquals(HttpStatus.OK, statusRes.getStatusCode());

        // Escalate trigger
        ResponseEntity<String> escRes = ticketController.triggerEscalation();
        assertEquals(HttpStatus.OK, escRes.getStatusCode());
        verify(manageTicketUseCase).checkAndEscalateTicketsNearingSlaBreach();
    }
}

