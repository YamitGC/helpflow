package com.riwi.support.application.service;

import com.riwi.support.application.mapper.TechnicianApplicationMapper;
import com.riwi.support.domain.exception.BusinessRuleException;
import com.riwi.support.domain.exception.ResourceNotFoundException;
import com.riwi.support.domain.model.Technician;
import com.riwi.support.domain.port.input.ManageTechnicianUseCase;
import com.riwi.support.domain.port.output.TechnicianRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ManageTechnicianService implements ManageTechnicianUseCase {

    private final TechnicianRepositoryPort technicianRepositoryPort;
    private final TechnicianApplicationMapper technicianMapper;

    @Override
    @Transactional
    public Technician registerTechnician(RegisterTechnicianCommand command) {
        if (technicianRepositoryPort.findByDocumento(command.documento()).isPresent()) {
            throw new BusinessRuleException("Ya existe un técnico con el documento: " + command.documento());
        }
        if (technicianRepositoryPort.findByCorreo(command.correo()).isPresent()) {
            throw new BusinessRuleException("Ya existe un técnico con el correo: " + command.correo());
        }

        Technician technician = technicianMapper.toDomain(command);
        return technicianRepositoryPort.save(technician);
    }

    @Override
    @Transactional
    public Technician updateTechnician(Long id, UpdateTechnicianCommand command) {
        Technician technician = technicianRepositoryPort.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Técnico no encontrado con ID: " + id));

        technicianMapper.updateDomainFromCommand(command, technician);
        return technicianRepositoryPort.save(technician);
    }

    @Override
    @Transactional(readOnly = true)
    public Technician getById(Long id) {
        return technicianRepositoryPort.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Técnico no encontrado con ID: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Technician> getAll() {
        return technicianRepositoryPort.findAll();
    }
}
