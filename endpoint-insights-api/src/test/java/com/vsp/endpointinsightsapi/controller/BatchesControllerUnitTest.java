package com.vsp.endpointinsightsapi.controller;

import tools.jackson.databind.ObjectMapper;
import com.vsp.endpointinsightsapi.dto.BatchRequestDTO;
import com.vsp.endpointinsightsapi.dto.BatchResponseDTO;
import com.vsp.endpointinsightsapi.exception.BatchNotFoundException;
import com.vsp.endpointinsightsapi.service.BatchService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@TestPropertySource(properties = "app.authentication.enabled=false")
@WebMvcTest(BatchesController.class)
@ActiveProfiles("test")
class BatchesControllerUnitTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private BatchService batchService;


    @Test
    void shouldReturnListOfBatches() throws Exception {
        mockMvc.perform(get("/api/batches"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$[0].id", notNullValue()))
                .andExpect(jsonPath("$[0].batchName", not(emptyString())))
                .andExpect(jsonPath("$[0].scheduleId", notNullValue()))
                .andExpect(jsonPath("$[0].active", anyOf(is(true), is(false))));
    }

    @Test
    void shouldReturnBatchById() throws Exception {
        UUID id = UUID.randomUUID();

        BatchResponseDTO dto = BatchResponseDTO.builder()
                .id(id)
                .batchName("Daily API Tests")
                .scheduleId(1001L)
                .startTime(LocalDate.parse("2025-11-08"))
                .lastTimeRun(LocalDate.parse("2025-11-09"))
                .active(true)
//                .jobs(Collections.emptyList()) Jobs not implemented yet
                .build();

        when(batchService.getBatchById(id)).thenReturn(dto);

        mockMvc.perform(get("/api/batches/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.batchName").value("Daily API Tests"))
                .andExpect(jsonPath("$.scheduleId").value(1001))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void getBatchById_notFound_shouldReturn404() throws Exception {
        UUID id = UUID.randomUUID();
        when(batchService.getBatchById(id)).thenThrow(new BatchNotFoundException(id.toString()));

        mockMvc.perform(get("/api/batches/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldCreateBatch() throws Exception {
        BatchRequestDTO request = new BatchRequestDTO(
                "New Batch",
                5001L,
                LocalDate.now(),
                true
        );

        mockMvc.perform(post("/api/batches")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.batchName", is("New Batch")))
                .andExpect(jsonPath("$.scheduleId", is(5001)))
                .andExpect(jsonPath("$.active", is(true)));
    }

    @Test
    void shouldUpdateBatch() throws Exception {
        BatchRequestDTO request = new BatchRequestDTO(
                "Updated Batch",
                7002L,
                LocalDate.now(),
                false
        );

        UUID id = UUID.randomUUID();

        mockMvc.perform(put("/api/batches/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(id.toString())))
                .andExpect(jsonPath("$.batchName", is("Updated Batch")))
                .andExpect(jsonPath("$.scheduleId", is(7002)))
                .andExpect(jsonPath("$.active", is(false)));
    }

    @Test
    void shouldDeleteBatch() throws Exception {
        UUID id = UUID.randomUUID();
        mockMvc.perform(delete("/api/batches/{id}", id))
                .andExpect(status().isNoContent());
    }
}
