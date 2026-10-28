package com.riwi.support.infrastructure.adapter.output.persistence.mapper;

import com.riwi.support.domain.model.Ticket;
import com.riwi.support.domain.model.TicketHistory;
import com.riwi.support.domain.model.TicketStatus;
import com.riwi.support.infrastructure.adapter.output.persistence.entity.TicketEntity;
import com.riwi.support.infrastructure.adapter.output.persistence.entity.TicketHistoryEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class TicketPersistenceMapperTest {

    private TicketPersistenceMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new TicketPersistenceMapper() {
            @Override
            public Ticket toDomain(TicketEntity entity) {
                return null;
            }

            @Override
            public TicketHistory toHistoryDomain(TicketHistoryEntity entity) {
                return null;
            }

            @Override
            public TicketEntity toEntity(Ticket domain) {
                return null;
            }
        };
    }

    @Test
    @DisplayName("stringToStatus debe convertir nombres válidos y retornar null ante nulo, vacío o inválido")
    void shouldConvertStringToStatusCorrectly() {
        assertEquals(TicketStatus.ABIERTO, mapper.stringToStatus("ABIERTO"));
        assertEquals(TicketStatus.ASIGNADO, mapper.stringToStatus("ASIGNADO"));
        assertEquals(TicketStatus.EN_PROGRESO, mapper.stringToStatus("EN_PROGRESO"));
        assertEquals(TicketStatus.RESUELTO, mapper.stringToStatus("RESUELTO"));
        assertEquals(TicketStatus.CERRADO, mapper.stringToStatus("CERRADO"));
        assertEquals(TicketStatus.CANCELADO, mapper.stringToStatus("CANCELADO"));

        assertNull(mapper.stringToStatus(null));
        assertNull(mapper.stringToStatus(""));
        assertNull(mapper.stringToStatus("   "));
        assertNull(mapper.stringToStatus("ESTADO_INEXISTENTE"));
    }

    @Test
    @DisplayName("mapHistoriales debe mapear la lista de historiales asociando la entidad padre")
    void shouldMapHistorialesCorrectly() {
        Ticket domain = Ticket.builder()
                .historiales(List.of(
                        TicketHistory.builder()
                                .id(1L)
                                .usuarioAccionId(10L)
                                .usuarioAccionNombre("Admin")
                                .estadoAnterior(TicketStatus.ABIERTO)
                                .estadoNuevo(TicketStatus.ASIGNADO)
                                .observaciones("Asignado a técnico")
                                .fechaCambio(LocalDateTime.now())
                                .build(),
                        TicketHistory.builder()
                                .id(2L)
                                .usuarioAccionId(null)
                                .usuarioAccionNombre("SISTEMA")
                                .estadoAnterior(null)
                                .estadoNuevo(null)
                                .observaciones(null)
                                .fechaCambio(LocalDateTime.now())
                                .build()
                ))
                .build();

        TicketEntity entity = new TicketEntity();
        entity.setHistoriales(new ArrayList<>());

        mapper.mapHistoriales(domain, entity);

        assertEquals(2, entity.getHistoriales().size());
        assertEquals("ABIERTO", entity.getHistoriales().get(0).getEstadoAnterior());
        assertEquals("ASIGNADO", entity.getHistoriales().get(0).getEstadoNuevo());
        assertNull(entity.getHistoriales().get(1).getEstadoAnterior());
        assertNull(entity.getHistoriales().get(1).getEstadoNuevo());
        assertEquals(entity, entity.getHistoriales().get(0).getTicket());
    }

    @Test
    @DisplayName("mapHistoriales no debe fallar si historiales es nulo")
    void shouldHandleNullHistoriales() {
        Ticket domain = Ticket.builder().historiales(null).build();
        TicketEntity entity = new TicketEntity();
        entity.setHistoriales(new ArrayList<>());

        mapper.mapHistoriales(domain, entity);
        assertEquals(0, entity.getHistoriales().size());
    }
}

