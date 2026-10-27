package com.riwi.support.domain.port.output;

import com.riwi.support.domain.model.Technician;
import com.riwi.support.domain.model.TicketCategory;

import java.util.List;
import java.util.Optional;

public interface TechnicianRepositoryPort {

    Technician save(Technician technician);

    Optional<Technician> findById(Long id);

    Optional<Technician> findByDocumento(String documento);

    Optional<Technician> findByCorreo(String correo);

    List<Technician> findAll();

    List<Technician> findByEspecialidadAndSede(String especialidad, String sede);

    List<Technician> findByEspecialidad(String especialidad);
}

