package com.riwi.support.infrastructure.adapter.output.persistence.mapper;

import com.riwi.support.domain.model.Ticket;
import com.riwi.support.domain.model.TicketHistory;
import com.riwi.support.domain.model.TicketStatus;
import com.riwi.support.infrastructure.adapter.output.persistence.entity.TicketEntity;
import com.riwi.support.infrastructure.adapter.output.persistence.entity.TicketHistoryEntity;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = "spring", uses = {UserPersistenceMapper.class, TechnicianPersistenceMapper.class})
public interface TicketPersistenceMapper {

    @Mapping(target = "historiales", source = "historiales")
    Ticket toDomain(TicketEntity entity);

    @Mapping(target = "ticketId", source = "ticket.id")
    @Mapping(target = "estadoAnterior", source = "estadoAnterior", qualifiedByName = "stringToStatus")
    @Mapping(target = "estadoNuevo", source = "estadoNuevo", qualifiedByName = "stringToStatus")
    TicketHistory toHistoryDomain(TicketHistoryEntity entity);

    @Mapping(target = "historiales", ignore = true) // Se gestionan en AfterMapping para asociar la entidad padre
    TicketEntity toEntity(Ticket domain);

    @AfterMapping
    default void mapHistoriales(Ticket domain, @MappingTarget TicketEntity entity) {
        if (domain.getHistoriales() != null) {
            List<TicketHistoryEntity> historyEntities = domain.getHistoriales().stream().map(h -> {
                TicketHistoryEntity historyEntity = new TicketHistoryEntity();
                historyEntity.setId(h.getId());
                historyEntity.setTicket(entity);
                historyEntity.setUsuarioAccionId(h.getUsuarioAccionId());
                historyEntity.setUsuarioAccionNombre(h.getUsuarioAccionNombre());
                historyEntity.setEstadoAnterior(h.getEstadoAnterior() != null ? h.getEstadoAnterior().name() : null);
                historyEntity.setEstadoNuevo(h.getEstadoNuevo() != null ? h.getEstadoNuevo().name() : null);
                historyEntity.setObservaciones(h.getObservaciones());
                historyEntity.setFechaCambio(h.getFechaCambio());
                return historyEntity;
            }).toList();
            entity.getHistoriales().clear();
            entity.getHistoriales().addAll(historyEntities);
        }
    }

    @Named("stringToStatus")
    default TicketStatus stringToStatus(String status) {
        if (status == null || status.isBlank()) return null;
        try {
            return TicketStatus.valueOf(status);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}

