package com.riwi.support.infrastructure.adapter.output.persistence.repository;

import com.riwi.support.domain.model.TicketCategory;
import com.riwi.support.domain.model.TicketPriority;
import com.riwi.support.domain.model.TicketStatus;
import com.riwi.support.infrastructure.adapter.output.persistence.entity.TicketEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface JpaTicketRepository extends JpaRepository<TicketEntity, Long> {

    // Evita problema N+1 cargando solicitante y técnico asignado con EntityGraph
    @EntityGraph(attributePaths = {"solicitante", "tecnicoAsignado"})
    Optional<TicketEntity> findWithDetailsById(Long id);

    @EntityGraph(attributePaths = {"solicitante", "tecnicoAsignado"})
    List<TicketEntity> findBySolicitanteId(Long solicitanteId);

    @EntityGraph(attributePaths = {"solicitante", "tecnicoAsignado"})
    List<TicketEntity> findByTecnicoAsignadoId(Long tecnicoId);

    @Override
    @EntityGraph(attributePaths = {"solicitante", "tecnicoAsignado"})
    List<TicketEntity> findAll();

    // Cuenta tickets activos (no resueltos ni cancelados) para controlar la carga máxima
    @Query("SELECT COUNT(t) FROM TicketEntity t WHERE t.tecnicoAsignado.id = :tecnicoId AND t.estado NOT IN ('RESUELTO', 'CERRADO', 'CANCELADO')")
    long countActiveTicketsByTecnicoId(@Param("tecnicoId") Long tecnicoId);

    long countByCategoria(TicketCategory categoria);

    long countByPrioridad(TicketPriority prioridad);

    // Consulta para escalamiento automático: tickets abiertos o asignados cuyo SLA vence en las próximas horas
    @Query("SELECT t FROM TicketEntity t WHERE t.estado IN ('ABIERTO', 'ASIGNADO', 'EN_PROGRESO') AND t.fechaLimiteSla <= :threshold")
    List<TicketEntity> findTicketsNearingSlaBreach(@Param("threshold") LocalDateTime threshold);
}

