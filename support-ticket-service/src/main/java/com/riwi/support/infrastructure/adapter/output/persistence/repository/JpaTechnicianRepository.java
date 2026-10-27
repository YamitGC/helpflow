package com.riwi.support.infrastructure.adapter.output.persistence.repository;

import com.riwi.support.infrastructure.adapter.output.persistence.entity.TechnicianEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JpaTechnicianRepository extends JpaRepository<TechnicianEntity, Long> {

    Optional<TechnicianEntity> findByDocumento(String documento);

    Optional<TechnicianEntity> findByCorreo(String correo);

    List<TechnicianEntity> findByEspecialidadAndSede(String especialidad, String sede);

    List<TechnicianEntity> findByEspecialidad(String especialidad);
}

