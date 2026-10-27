package com.riwi.support.infrastructure.adapter.output.persistence.adapter;

import com.riwi.support.domain.model.Technician;
import com.riwi.support.domain.port.output.TechnicianRepositoryPort;
import com.riwi.support.infrastructure.adapter.output.persistence.entity.TechnicianEntity;
import com.riwi.support.infrastructure.adapter.output.persistence.mapper.TechnicianPersistenceMapper;
import com.riwi.support.infrastructure.adapter.output.persistence.repository.JpaTechnicianRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class TechnicianRepositoryAdapter implements TechnicianRepositoryPort {

    private final JpaTechnicianRepository jpaTechnicianRepository;
    private final TechnicianPersistenceMapper technicianMapper;

    @Override
    public Technician save(Technician technician) {
        TechnicianEntity entity = technicianMapper.toEntity(technician);
        TechnicianEntity saved = jpaTechnicianRepository.save(entity);
        return technicianMapper.toDomain(saved);
    }

    @Override
    public Optional<Technician> findById(Long id) {
        return jpaTechnicianRepository.findById(id).map(technicianMapper::toDomain);
    }

    @Override
    public Optional<Technician> findByDocumento(String documento) {
        return jpaTechnicianRepository.findByDocumento(documento).map(technicianMapper::toDomain);
    }

    @Override
    public Optional<Technician> findByCorreo(String correo) {
        return jpaTechnicianRepository.findByCorreo(correo).map(technicianMapper::toDomain);
    }

    @Override
    public List<Technician> findAll() {
        return jpaTechnicianRepository.findAll().stream()
                .map(technicianMapper::toDomain)
                .toList();
    }

    @Override
    public List<Technician> findByEspecialidadAndSede(String especialidad, String sede) {
        return jpaTechnicianRepository.findByEspecialidadAndSede(especialidad, sede).stream()
                .map(technicianMapper::toDomain)
                .toList();
    }

    @Override
    public List<Technician> findByEspecialidad(String especialidad) {
        return jpaTechnicianRepository.findByEspecialidad(especialidad).stream()
                .map(technicianMapper::toDomain)
                .toList();
    }
}

