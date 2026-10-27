package com.riwi.support.domain.port.input;

import com.riwi.support.domain.model.Technician;
import com.riwi.support.domain.model.UserStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface ManageTechnicianUseCase {

    Technician registerTechnician(RegisterTechnicianCommand command);

    Technician updateTechnician(Long id, UpdateTechnicianCommand command);

    Technician getById(Long id);

    List<Technician> getAll();

    record RegisterTechnicianCommand(
            String documento,
            String nombre,
            String correo,
            BigDecimal salario,
            LocalDate fechaAfiliacion,
            String especialidad,
            String sede,
            Integer maxTicketsActivos
    ) {}

    record UpdateTechnicianCommand(
            String nombre,
            BigDecimal salario,
            String especialidad,
            String sede,
            UserStatus estado,
            Integer maxTicketsActivos
    ) {}
}

