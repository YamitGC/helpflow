package com.riwi.support.infrastructure.adapter.output.client;

import com.riwi.support.domain.model.TicketCategory;
import com.riwi.support.domain.model.TicketPriority;
import com.riwi.support.domain.port.output.TechnicianDispatchPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
public class TechnicianDispatchRestClientAdapter implements TechnicianDispatchPort {

    private final RestClient restClient;

    public TechnicianDispatchRestClientAdapter(
            @Value("${services.technician-dispatch.url:http://localhost:8081}") String dispatchBaseUrl) {
        log.info("Inicializando cliente HTTP hacia technician-dispatch-service en URL: {}", dispatchBaseUrl);
        this.restClient = RestClient.builder()
                .baseUrl(dispatchBaseUrl)
                .build();
    }

    private record ClientDispatchRequest(Long ticketId, String categoria, String prioridad) {}
    private record ClientDispatchResponse(Long ticketId, String tecnicoId, String disponibilidad, Integer tiempoEstimadoHoras) {}

    @Override
    public DispatchResult requestTechnician(Long ticketId, TicketCategory categoria, TicketPriority prioridad) {
        try {
            ClientDispatchRequest request = new ClientDispatchRequest(
                    ticketId,
                    categoria != null ? categoria.name() : "OTRO",
                    prioridad != null ? prioridad.name() : "MEDIA"
            );

            log.info("Enviando petición de dispatch para ticket ID {}: {}", ticketId, request);

            ClientDispatchResponse response = restClient.post()
                    .uri("/technician-dispatch")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(ClientDispatchResponse.class);

            if (response != null) {
                return new DispatchResult(
                        response.ticketId(),
                        response.tecnicoId(),
                        response.disponibilidad(),
                        response.tiempoEstimadoHoras()
                );
            }
        } catch (Exception e) {
            log.error("Error al comunicarse con technician-dispatch-service: {}", e.getMessage());
        }

        // Fallback determinista en caso de que el microservicio externo no responda
        return new DispatchResult(ticketId, "TEC-001", "MEDIA", 4);
    }
}

