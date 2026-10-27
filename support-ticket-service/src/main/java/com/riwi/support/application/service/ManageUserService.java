package com.riwi.support.application.service;

import com.riwi.support.application.mapper.UserApplicationMapper;
import com.riwi.support.domain.exception.BusinessRuleException;
import com.riwi.support.domain.exception.ResourceNotFoundException;
import com.riwi.support.domain.model.User;
import com.riwi.support.domain.port.input.ManageUserUseCase;
import com.riwi.support.domain.port.output.UserRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ManageUserService implements ManageUserUseCase {

    private final UserRepositoryPort userRepositoryPort;
    private final PasswordEncoder passwordEncoder;
    private final UserApplicationMapper userMapper;

    @Override
    @Transactional
    public User registerUser(RegisterUserCommand command) {
        if (userRepositoryPort.existsByDocumento(command.documento())) {
            throw new BusinessRuleException("Ya existe un usuario con el documento: " + command.documento());
        }
        if (userRepositoryPort.existsByCorreo(command.correo())) {
            throw new BusinessRuleException("Ya existe un usuario con el correo: " + command.correo());
        }

        User user = userMapper.toDomain(command);
        user.setPassword(passwordEncoder.encode(command.password()));

        return userRepositoryPort.save(user);
    }

    @Override
    @Transactional
    public User updateProfile(Long userId, UpdateProfileCommand command) {
        User user = userRepositoryPort.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + userId));

        userMapper.updateDomainFromCommand(command, user);

        return userRepositoryPort.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public User getById(Long id) {
        return userRepositoryPort.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public User getByCorreo(String correo) {
        return userRepositoryPort.findByCorreo(correo)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con correo: " + correo));
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userRepositoryPort.findAll();
    }
}
