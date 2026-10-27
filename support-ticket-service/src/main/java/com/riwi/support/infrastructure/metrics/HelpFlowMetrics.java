package com.riwi.support.infrastructure.metrics;

import com.riwi.support.domain.model.TicketCategory;
import com.riwi.support.domain.model.TicketPriority;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Observabilidad y métricas de negocio con Micrometer según enunciado:
 * - tiempo medio de primera respuesta y resolución
 * - tickets vencidos según SLA
 * - tickets creados por categoría y prioridad
 * - fallas de autenticación y accesos denegados
 */
@Component
public class HelpFlowMetrics {

    private final MeterRegistry registry;

    private final Counter authFailuresCounter;
    private final Counter accessDeniedCounter;
    private final Counter expiredTicketsCounter;

    private final Timer firstResponseTimer;
    private final Timer resolutionTimer;

    private final ConcurrentMap<String, Counter> createdTicketsCounters = new ConcurrentHashMap<>();

    public HelpFlowMetrics(MeterRegistry registry) {
        this.registry = registry;

        this.authFailuresCounter = Counter.builder("helpflow_auth_failures_total")
                .description("Total de fallas de autenticación")
                .register(registry);

        this.accessDeniedCounter = Counter.builder("helpflow_access_denied_total")
                .description("Total de accesos denegados a recursos")
                .register(registry);

        this.expiredTicketsCounter = Counter.builder("helpflow_tickets_expired_total")
                .description("Total de tickets que han sobrepasado su SLA")
                .register(registry);

        this.firstResponseTimer = Timer.builder("helpflow_ticket_first_response_duration")
                .description("Tiempo transcurrido hasta la primera respuesta técnica")
                .register(registry);

        this.resolutionTimer = Timer.builder("helpflow_ticket_resolution_duration")
                .description("Tiempo total transcurrido hasta la resolución del ticket")
                .register(registry);
    }

    public void incrementCreatedTickets(TicketCategory categoria, TicketPriority prioridad) {
        String key = (categoria != null ? categoria.name() : "OTRO") + "_"
                + (prioridad != null ? prioridad.name() : "MEDIA");
        createdTicketsCounters.computeIfAbsent(key, k -> Counter.builder("helpflow_tickets_created_total")
                .description("Total de tickets creados por categoría y prioridad")
                .tag("categoria", categoria != null ? categoria.name() : "OTRO")
                .tag("prioridad", prioridad != null ? prioridad.name() : "MEDIA")
                .register(registry)).increment();
    }

    public void incrementAuthFailure() {
        authFailuresCounter.increment();
    }

    public void incrementAccessDenied() {
        accessDeniedCounter.increment();
    }

    public void incrementExpiredTickets() {
        expiredTicketsCounter.increment();
    }

    public void recordFirstResponseTime(Duration duration) {
        if (duration != null && !duration.isNegative()) {
            firstResponseTimer.record(duration);
        }
    }

    public void recordResolutionTime(Duration duration) {
        if (duration != null && !duration.isNegative()) {
            resolutionTimer.record(duration);
        }
    }
}
