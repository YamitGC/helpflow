package com.riwi.support.application.service;

import com.riwi.support.application.mapper.TechnicianApplicationMapper;
import com.riwi.support.domain.exception.BusinessRuleException;
import com.riwi.support.domain.exception.ResourceNotFoundException;
import com.riwi.support.domain.model.Technician;
import com.riwi.support.domain.model.UserStatus;
import com.riwi.support.domain.port.input.ManageTechnicianUseCase;
import com.riwi.support.domain.port.output.TechnicianRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ManageTechnicianServiceTest {

    @Mock
    private TechnicianRepositoryPort technicianRepositoryPort;

    @Mock
    private TechnicianApplicationMapper technicianMapper;

    @InjectMocks
    private ManageTechnicianService manageTechnicianService;

    private Technician technician;

    @BeforeEach
    void setUp() {
        technician = Technician.builder()
                .id(1L)
                .documento("TEC-100")
                .nombre("Pedro Redes")
                .correo("pedro@helpflow.com")
                .salario(new BigDecimal("3000000"))
                .fechaAfiliacion(LocalDate.now())
                .especialidad("RED")
                .sede("Medellin")
                .estado(UserStatus.ACTIVO)
                .maxTicketsActivos(5)
                .build();
    }

    @Test
    @DisplayName("Debe registrar un técnico exitosamente")
    void shouldRegisterTechnicianSuccessfully() {
        ManageTechnicianUseCase.RegisterTechnicianCommand command = new ManageTechnicianUseCase.RegisterTechnicianCommand(
                "TEC-100", "Pedro Redes", "pedro@helpflow.com", new BigDecimal("3000000"),
                LocalDate.now(), "RED", "Medellin", 5
        );

        when(technicianRepositoryPort.findByDocumento("TEC-100")).thenReturn(Optional.empty());
        when(technicianRepositoryPort.findByCorreo("pedro@helpflow.com")).thenReturn(Optional.empty());
        when(technicianMapper.toDomain(command)).thenReturn(technician);
        when(technicianRepositoryPort.save(any(Technician.class))).thenReturn(technician);

        Technician saved = manageTechnicianService.registerTechnician(command);

        assertNotNull(saved);
        assertEquals("TEC-100", saved.getDocumento());
        verify(technicianRepositoryPort).save(technician);
    }

    @Test
    @DisplayName("Debe fallar si el correo del técnico ya está en uso")
    void shouldThrowExceptionWhenTechnicianEmailExists() {
        ManageTechnicianUseCase.RegisterTechnicianCommand command = new ManageTechnicianUseCase.RegisterTechnicianCommand(
                "TEC-100", "Pedro", "pedro@helpflow.com", BigDecimal.TEN, LocalDate.now(), "RED", "Med", 5
        );
        when(technicianRepositoryPort.findByDocumento("TEC-100")).thenReturn(Optional.empty());
        when(technicianRepositoryPort.findByCorreo("pedro@helpflow.com")).thenReturn(Optional.of(technician));

        assertThrows(BusinessRuleException.class, () -> manageTechnicianService.registerTechnician(command));
    }

    @Test
    @DisplayName("Debe actualizar información del técnico")
    void shouldUpdateTechnician() {
        ManageTechnicianUseCase.UpdateTechnicianCommand command = new ManageTechnicianUseCase.UpdateTechnicianCommand(
                "Pedro Modificado", new BigDecimal("3500000"), "RED", "Bogota", UserStatus.ACTIVO, 6
        );
        when(technicianRepositoryPort.findById(1L)).thenReturn(Optional.of(technician));
        when(technicianRepositoryPort.save(technician)).thenReturn(technician);

        Technician updated = manageTechnicianService.updateTechnician(1L, command);

        assertNotNull(updated);
        verify(technicianMapper).updateDomainFromCommand(command, technician);
        verify(technicianRepositoryPort).save(technician);
    }

    @Test
    @DisplayName("Debe listar todos los técnicos y obtener por ID")
    void shouldFindAllAndById() {
        when(technicianRepositoryPort.findAll()).thenReturn(List.of(technician));
        when(technicianRepositoryPort.findById(1L)).thenReturn(Optional.of(technician));
        when(technicianRepositoryPort.findById(99L)).thenReturn(Optional.empty());

        assertEquals(1, manageTechnicianService.getAll().size());
        assertNotNull(manageTechnicianService.getById(1L));
        assertThrows(ResourceNotFoundException.class, () -> manageTechnicianService.getById(99L));
    }
}

