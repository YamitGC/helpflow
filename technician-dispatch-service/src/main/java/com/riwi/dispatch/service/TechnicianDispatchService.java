package com.riwi.dispatch.service;

import com.riwi.dispatch.dto.DispatchRequest;
import com.riwi.dispatch.dto.DispatchResponse;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Random;

@Service
public class TechnicianDispatchService {

    /**
     * Reglas del enunciado:
     * 1. Respuesta determinista para ticketId + categoria + prioridad.
     * 2. Misma combinación -> mismo técnico y tiempo.
     * 3. Clasificación de disponibilidad:
     *    - 0 a 2 horas  -> DISPONIBILIDAD ALTA
     *    - 3 a 8 horas  -> DISPONIBILIDAD MEDIA
     *    - Más de 8 horas -> DISPONIBILIDAD BAJA
     */
    public DispatchResponse dispatch(DispatchRequest request) {
        // Generar seed determinista a partir de los datos de entrada
        String compositeKey = String.format("%d-%s-%s",
                request.getTicketId(),
                request.getCategoria().trim().toUpperCase(),
                request.getPrioridad().trim().toUpperCase());

        long seed = (long) Objects.hash(compositeKey);
        Random deterministicRandom = new Random(seed);

        // Técnico determinista entre TEC-001 y TEC-020
        int techNumber = 1 + deterministicRandom.nextInt(20);
        String tecnicoId = String.format("TEC-%03d", techNumber);

        // Tiempo estimado en horas (entre 1 y 12 horas)
        int tiempoEstimadoHoras = 1 + deterministicRandom.nextInt(12);

        // Clasificación de disponibilidad requerida por el enunciado
        String disponibilidad;
        if (tiempoEstimadoHoras <= 2) {
            disponibilidad = "ALTA";
        } else if (tiempoEstimadoHoras <= 8) {
            disponibilidad = "MEDIA";
        } else {
            disponibilidad = "BAJA";
        }

        return DispatchResponse.builder()
                .ticketId(request.getTicketId())
                .tecnicoId(tecnicoId)
                .disponibilidad(disponibilidad)
                .tiempoEstimadoHoras(tiempoEstimadoHoras)
                .build();
    }
}

