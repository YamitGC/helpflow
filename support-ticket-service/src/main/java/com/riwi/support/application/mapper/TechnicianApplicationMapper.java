package com.riwi.support.application.mapper;

import com.riwi.support.domain.model.Technician;
import com.riwi.support.domain.port.input.ManageTechnicianUseCase;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface TechnicianApplicationMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "estado", constant = "ACTIVO")
    @Mapping(target = "maxTicketsActivos", defaultValue = "5")
    @Mapping(target = "createdAt", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "updatedAt", expression = "java(java.time.LocalDateTime.now())")
    Technician toDomain(ManageTechnicianUseCase.RegisterTechnicianCommand command);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "documento", ignore = true)
    @Mapping(target = "correo", ignore = true)
    @Mapping(target = "fechaAfiliacion", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", expression = "java(java.time.LocalDateTime.now())")
    void updateDomainFromCommand(ManageTechnicianUseCase.UpdateTechnicianCommand command, @MappingTarget Technician technician);
}

