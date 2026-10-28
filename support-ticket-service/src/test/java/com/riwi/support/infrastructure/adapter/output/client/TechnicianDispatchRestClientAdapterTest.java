package com.riwi.support.infrastructure.adapter.output.client;

import com.riwi.support.domain.model.TicketCategory;
import com.riwi.support.domain.model.TicketPriority;
import com.riwi.support.domain.port.output.TechnicianDispatchPort.DispatchResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class TechnicianDispatchRestClientAdapterTest {

    private TechnicianDispatchRestClientAdapter adapter;
    private MockRestServiceServer mockServer;

    @BeforeEach
    void setUp() {
        adapter = new TechnicianDispatchRestClientAdapter("http://localhost:8081");
        RestClient.Builder builder = RestClient.builder().baseUrl("http://localhost:8081");
        mockServer = MockRestServiceServer.bindTo(builder).build();
        ReflectionTestUtils.setField(adapter, "restClient", builder.build());
    }

    @Test
    @DisplayName("Debe procesar respuesta exitosa de technician-dispatch-service")
    void shouldReturnDispatchResultOnSuccessfulResponse() {
        String jsonResponse = """
                {
                    "ticketId": 10,
                    "tecnicoId": "TEC-007",
                    "disponibilidad": "ALTA",
                    "tiempoEstimadoHoras": 1
                }
                """;

        mockServer.expect(requestTo("http://localhost:8081/technician-dispatch"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(jsonResponse, MediaType.APPLICATION_JSON));

        DispatchResult result = adapter.requestTechnician(10L, TicketCategory.HARDWARE, TicketPriority.ALTA);

        mockServer.verify();
        assertNotNull(result);
        assertEquals(10L, result.ticketId());
        assertEquals("TEC-007", result.tecnicoId());
        assertEquals("ALTA", result.disponibilidad());
        assertEquals(1, result.tiempoEstimadoHoras());
    }

    @Test
    @DisplayName("Debe retornar fallback determinista si ocurre un error HTTP 500 en el microservicio")
    void shouldReturnFallbackOnServerError() {
        mockServer.expect(requestTo("http://localhost:8081/technician-dispatch"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withServerError());

        DispatchResult result = adapter.requestTechnician(20L, TicketCategory.RED, TicketPriority.CRITICA);

        mockServer.verify();
        assertNotNull(result);
        assertEquals(20L, result.ticketId());
        assertEquals("TEC-001", result.tecnicoId());
        assertEquals("MEDIA", result.disponibilidad());
        assertEquals(4, result.tiempoEstimadoHoras());
    }

    @Test
    @DisplayName("Debe manejar categoría o prioridad nula enviando valores por defecto y retornando fallback")
    void shouldHandleNullCategoryAndPriority() {
        // Adaptador con URL inaccesible para forzar excepción de red directa
        TechnicianDispatchRestClientAdapter brokenAdapter = new TechnicianDispatchRestClientAdapter("http://localhost:59999");
        DispatchResult result = brokenAdapter.requestTechnician(30L, null, null);

        assertNotNull(result);
        assertEquals(30L, result.ticketId());
        assertEquals("TEC-001", result.tecnicoId());
        assertEquals("MEDIA", result.disponibilidad());
    }
}

