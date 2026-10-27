package com.riwi.support.infrastructure.adapter.input.rest.mapper;

import com.riwi.support.domain.model.Technician;
import com.riwi.support.domain.model.Ticket;
import com.riwi.support.domain.model.TicketHistory;
import com.riwi.support.domain.model.User;
import com.riwi.support.domain.port.input.ManageTechnicianUseCase;
import com.riwi.support.domain.port.input.ManageTicketUseCase;
import com.riwi.support.domain.port.input.ManageUserUseCase;
import com.riwi.support.infrastructure.adapter.input.rest.dto.request.CreateTicketRequest;
import com.riwi.support.infrastructure.adapter.input.rest.dto.request.RegisterRequest;
import com.riwi.support.infrastructure.adapter.input.rest.dto.request.TechnicianRequest;
import com.riwi.support.infrastructure.adapter.input.rest.dto.response.TechnicianResponse;
import com.riwi.support.infrastructure.adapter.input.rest.dto.response.TicketResponse;
import com.riwi.support.infrastructure.adapter.input.rest.dto.response.UserResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RestDtoMapper {

    ManageUserUseCase.RegisterUserCommand toCommand(RegisterRequest request);

    UserResponse toResponse(User user);

    ManageTechnicianUseCase.RegisterTechnicianCommand toCommand(TechnicianRequest request);

    TechnicianResponse toResponse(Technician technician);

    @Mapping(target = "solicitanteId", source = "solicitanteId")
    ManageTicketUseCase.CreateTicketCommand toCommand(CreateTicketRequest request, Long solicitanteId);

    @Mapping(target = "solicitanteId", source = "solicitante.id")
    @Mapping(target = "solicitanteNombre", source = "solicitante.nombre")
    @Mapping(target = "solicitanteCorreo", source = "solicitante.correo")
    @Mapping(target = "tecnicoId", source = "tecnicoAsignado.id")
    @Mapping(target = "tecnicoNombre", source = "tecnicoAsignado.nombre")
    @Mapping(target = "tecnicoEspecialidad", source = "tecnicoAsignado.especialidad")
    @Mapping(target = "vencido", expression = "java(ticket.isVencido())")
    @Mapping(target = "historiales", source = "historiales")
    TicketResponse toResponse(Ticket ticket);

    @Mapping(target = "estadoAnterior", expression = "java(history.getEstadoAnterior() != null ? history.getEstadoAnterior().name() : null)")
    @Mapping(target = "estadoNuevo", expression = "java(history.getEstadoNuevo() != null ? history.getEstadoNuevo().name() : null)")
    TicketResponse.HistoryResponse toHistoryResponse(TicketHistory history);
}

