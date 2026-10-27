package com.riwi.support.infrastructure.metrics;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static com.riwi.support.domain.model.TicketCategory.RED;
import static com.riwi.support.domain.model.TicketPriority.ALTA;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class HelpFlowMetricsTest {

    @Test
    @DisplayName("Debe registrar y actualizar todas las métricas de negocio sin fallos")
    void testMetrics() {
        HelpFlowMetrics metrics = new HelpFlowMetrics(new SimpleMeterRegistry());

        assertDoesNotThrow(() -> {
            metrics.incrementCreatedTickets(RED, ALTA);
            metrics.incrementAuthFailure();
            metrics.incrementAccessDenied();
            metrics.incrementExpiredTickets();
            metrics.recordFirstResponseTime(Duration.ofMinutes(15));
            metrics.recordResolutionTime(Duration.ofHours(2));
        });
    }
}

