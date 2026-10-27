package com.riwi.support.infrastructure.adapter.output.persistence.mapper;

import com.riwi.support.domain.model.User;
import com.riwi.support.infrastructure.adapter.output.persistence.entity.UserEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserPersistenceMapper {

    User toDomain(UserEntity entity);

    UserEntity toEntity(User domain);
}

