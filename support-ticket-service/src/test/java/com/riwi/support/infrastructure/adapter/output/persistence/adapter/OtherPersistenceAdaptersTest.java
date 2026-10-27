package com.riwi.support.infrastructure.adapter.output.persistence.adapter;

import com.riwi.support.domain.model.Technician;
import com.riwi.support.domain.model.Ticket;
import com.riwi.support.domain.model.TicketCategory;
import com.riwi.support.domain.model.TicketPriority;
import com.riwi.support.infrastructure.adapter.output.persistence.entity.TechnicianEntity;
import com.riwi.support.infrastructure.adapter.output.persistence.entity.TicketEntity;
import com.riwi.support.infrastructure.adapter.output.persistence.mapper.TechnicianPersistenceMapper;
import com.riwi.support.infrastructure.adapter.output.persistence.mapper.TicketPersistenceMapper;
import com.riwi.support.infrastructure.adapter.output.persistence.repository.JpaTechnicianRepository;
import com.riwi.support.infrastructure.adapter.output.persistence.repository.JpaTicketRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OtherPersistenceAdaptersTest {

    @Mock
    private JpaTechnicianRepository jpaTechnicianRepository;
    @Mock
    private TechnicianPersistenceMapper technicianMapper;
    @InjectMocks
    private TechnicianRepositoryAdapter technicianRepositoryAdapter;

    @Mock
    private JpaTicketRepository jpaTicketRepository;
    @Mock
    private TicketPersistenceMapper ticketMapper;
    @InjectMocks
    private TicketRepositoryAdapter ticketRepositoryAdapter;

    @Test
    @DisplayName("TechnicianRepositoryAdapter: save, findById, findAll, findByEspecialidad")
    void testTechnicianAdapter() {
        Technician domain = Technician.builder().id(1L).documento("TEC-1").correo("t@h.com").build();
        TechnicianEntity entity = TechnicianEntity.builder().id(1L).documento("TEC-1").correo("t@h.com").build();

        when(technicianMapper.toEntity(domain)).thenReturn(entity);
        when(jpaTechnicianRepository.save(entity)).thenReturn(entity);
        when(technicianMapper.toDomain(entity)).thenReturn(domain);

        Technician saved = technicianRepositoryAdapter.save(domain);
        assertNotNull(saved);
        verify(jpaTechnicianRepository).save(entity);

        when(jpaTechnicianRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(jpaTechnicianRepository.findByDocumento("TEC-1")).thenReturn(Optional.of(entity));
        when(jpaTechnicianRepository.findByCorreo("t@h.com")).thenReturn(Optional.of(entity));
        when(jpaTechnicianRepository.findAll()).thenReturn(List.of(entity));
        when(jpaTechnicianRepository.findByEspecialidad("RED")).thenReturn(List.of(entity));
        when(jpaTechnicianRepository.findByEspecialidadAndSede("RED", "Med")).thenReturn(List.of(entity));

        assertTrue(technicianRepositoryAdapter.findById(1L).isPresent());
        assertTrue(technicianRepositoryAdapter.findByDocumento("TEC-1").isPresent());
        assertTrue(technicianRepositoryAdapter.findByCorreo("t@h.com").isPresent());
        assertEquals(1, technicianRepositoryAdapter.findAll().size());
        assertEquals(1, technicianRepositoryAdapter.findByEspecialidad("RED").size());
        assertEquals(1, technicianRepositoryAdapter.findByEspecialidadAndSede("RED", "Med").size());
    }

    @Test
    @DisplayName("TicketRepositoryAdapter: save, findById, counts")
    void testTicketAdapter() {
        Ticket domain = Ticket.builder().id(10L).build();
        TicketEntity entity = TicketEntity.builder().id(10L).build();

        when(ticketMapper.toEntity(domain)).thenReturn(entity);
        when(jpaTicketRepository.save(entity)).thenReturn(entity);
        when(ticketMapper.toDomain(entity)).thenReturn(domain);

        Ticket saved = ticketRepositoryAdapter.save(domain);
        assertNotNull(saved);

        when(jpaTicketRepository.findWithDetailsById(10L)).thenReturn(Optional.of(entity));
        when(jpaTicketRepository.findBySolicitanteId(1L)).thenReturn(List.of(entity));
        when(jpaTicketRepository.findByTecnicoAsignadoId(2L)).thenReturn(List.of(entity));
        when(jpaTicketRepository.findAll()).thenReturn(List.of(entity));
        when(jpaTicketRepository.countActiveTicketsByTecnicoId(2L)).thenReturn(3L);
        when(jpaTicketRepository.countByCategoria(TicketCategory.RED)).thenReturn(5L);
        when(jpaTicketRepository.countByPrioridad(TicketPriority.ALTA)).thenReturn(2L);
        when(jpaTicketRepository.findTicketsNearingSlaBreach(any())).thenReturn(List.of(entity));

        assertTrue(ticketRepositoryAdapter.findById(10L).isPresent());
        assertEquals(1, ticketRepositoryAdapter.findBySolicitanteId(1L).size());
        assertEquals(1, ticketRepositoryAdapter.findByTecnicoAsignadoId(2L).size());
        assertEquals(1, ticketRepositoryAdapter.findAll().size());
        assertEquals(3L, ticketRepositoryAdapter.countActiveTicketsByTecnicoId(2L));
        assertEquals(5L, ticketRepositoryAdapter.countByCategoria(TicketCategory.RED));
        assertEquals(2L, ticketRepositoryAdapter.countByPrioridad(TicketPriority.ALTA));
        assertEquals(1, ticketRepositoryAdapter.findTicketsNearingSlaBreach().size());
    }
}
