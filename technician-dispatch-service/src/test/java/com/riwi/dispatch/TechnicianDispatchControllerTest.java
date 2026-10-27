package com.riwi.dispatch;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.riwi.dispatch.dto.DispatchRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class TechnicianDispatchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Debe retornar técnico determinista para la misma combinación de entrada")
    void shouldReturnDeterministicTechnician() throws Exception {
        DispatchRequest request = DispatchRequest.builder()
                .ticketId(125L)
                .categoria("RED")
                .prioridad("ALTA")
                .build();

        String jsonPayload = objectMapper.writeValueAsString(request);

        // Primera llamada
        String firstResponse = mockMvc.perform(post("/technician-dispatch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ticketId").value(125))
                .andExpect(jsonPath("$.tecnicoId").isNotEmpty())
                .andExpect(jsonPath("$.disponibilidad").isNotEmpty())
                .andExpect(jsonPath("$.tiempoEstimadoHoras").isNumber())
                .andReturn().getResponse().getContentAsString();

        // Segunda llamada con exactamente los mismos datos (debe ser determinista e idéntica)
        mockMvc.perform(post("/technician-dispatch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isOk())
                .andExpect(result -> org.junit.jupiter.api.Assertions.assertEquals(
                        firstResponse,
                        result.getResponse().getContentAsString()
                ));
    }
}

