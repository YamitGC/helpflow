package com.riwi.support.infrastructure.adapter.output.persistence.mapper;

import com.riwi.support.domain.model.Technician;
import com.riwi.support.infrastructure.adapter.output.persistence.entity.TechnicianEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TechnicianPersistenceMapper {

    Technician toDomain(TechnicianEntity entity);

    TechnicianEntity toEntity(Technician domain);
}

