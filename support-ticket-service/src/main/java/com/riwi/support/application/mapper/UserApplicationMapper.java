package com.riwi.support.application.mapper;

import com.riwi.support.domain.model.User;
import com.riwi.support.domain.port.input.ManageUserUseCase;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface UserApplicationMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true) // La contraseña se cifra con PasswordEncoder antes o se pasa por separado
    @Mapping(target = "estado", constant = "ACTIVO")
    @Mapping(target = "createdAt", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "updatedAt", expression = "java(java.time.LocalDateTime.now())")
    User toDomain(ManageUserUseCase.RegisterUserCommand command);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "documento", ignore = true)
    @Mapping(target = "correo", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "rol", ignore = true)
    @Mapping(target = "estado", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", expression = "java(java.time.LocalDateTime.now())")
    void updateDomainFromCommand(ManageUserUseCase.UpdateProfileCommand command, @MappingTarget User user);
}

