package com.riwi.support.application.mapper;

import com.riwi.support.domain.model.Ticket;
import com.riwi.support.domain.model.TicketHistory;
import com.riwi.support.domain.model.TicketStatus;
import com.riwi.support.domain.model.User;
import com.riwi.support.domain.port.input.ManageTicketUseCase;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.LocalDateTime;

@Mapper(componentModel = "spring")
public interface TicketApplicationMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "estado", constant = "ABIERTO")
    @Mapping(target = "fechaPrimeraRespuesta", ignore = true)
    @Mapping(target = "fechaResolucion", ignore = true)
    @Mapping(target = "tecnicoAsignado", ignore = true)
    @Mapping(target = "historiales", expression = "java(new java.util.ArrayList<>())")
    @Mapping(target = "solicitante", source = "solicitante")
    @Mapping(target = "fechaCreacion", source = "fechaCreacion")
    @Mapping(target = "fechaLimiteSla", source = "fechaLimiteSla")
    @Mapping(target = "sede", expression = "java(command.sede() != null ? command.sede() : solicitante.getSede())")
    Ticket toDomain(ManageTicketUseCase.CreateTicketCommand command, User solicitante, LocalDateTime fechaCreacion, LocalDateTime fechaLimiteSla);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ticketId", source = "ticket.id")
    @Mapping(target = "usuarioAccionId", source = "usuarioId")
    @Mapping(target = "usuarioAccionNombre", source = "usuarioNombre")
    @Mapping(target = "estadoAnterior", source = "estadoAnterior")
    @Mapping(target = "estadoNuevo", source = "estadoNuevo")
    @Mapping(target = "observaciones", source = "observaciones")
    @Mapping(target = "fechaCambio", source = "fechaCambio")
    TicketHistory toHistory(Ticket ticket, Long usuarioId, String usuarioNombre, TicketStatus estadoAnterior, TicketStatus estadoNuevo, String observaciones, LocalDateTime fechaCambio);
}

