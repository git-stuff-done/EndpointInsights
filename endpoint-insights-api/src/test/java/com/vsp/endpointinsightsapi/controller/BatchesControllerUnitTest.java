package com.vsp.endpointinsightsapi.controller;

import tools.jackson.databind.ObjectMapper;
import com.vsp.endpointinsightsapi.dto.BatchRequestDTO;
import com.vsp.endpointinsightsapi.model.TestBatch;
import com.vsp.endpointinsightsapi.service.BatchService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@TestPropertySource(properties = "app.authentication.enabled=false")
@WebMvcTest(controllers = BatchesController.class)
class BatchesControllerUnitTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockitoBean
    private BatchService batchService;

    @Test
    void shouldReturnListOfBatches() throws Exception {
        mockMvc.perform(get("/api/batches"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$[0].id", notNullValue()))
                .andExpect(jsonPath("$[0].name", not(emptyString())))
                .andExpect(jsonPath("$[0].status", not(emptyString())));
    }

    @Test
    void shouldReturnBatchById() throws Exception {
        UUID id = UUID.randomUUID();
        TestBatch batch = new TestBatch(id, null, "Example Batch",
                1L, LocalDate.now(), LocalDate.now(), true);

        Mockito.when(batchService.getBatchById(any(UUID.class)))
                .thenReturn(Optional.of(batch));

        mockMvc.perform(get("/api/batches/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.batchName", is("Example Batch")))
                .andExpect(jsonPath("$.active", is(true)));
    }

    @Test
    void shouldReturnNotFoundWhenBatchMissing() throws Exception {
        UUID id = UUID.randomUUID();

        Mockito.when(batchService.getBatchById(any(UUID.class)))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/api/batches/" + id))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldCreateBatch() throws Exception {
        BatchRequestDTO request = new BatchRequestDTO("New Batch", Collections.emptyList());
        TestBatch batch = new TestBatch();
        batch.setBatchName("New Batch");

        Mockito.when(batchService.createBatch(Mockito.any(BatchRequestDTO.class)))
                .thenReturn(batch);

        mockMvc.perform(post("/api/batches")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.batchName", is("New Batch")));
    }

    @Test
    void shouldUpdateBatch() throws Exception {
        BatchRequestDTO request = new BatchRequestDTO("Updated Batch", Collections.emptyList());

        mockMvc.perform(put("/api/batches/2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(2)))
                .andExpect(jsonPath("$.name", is("Updated Batch")))
                .andExpect(jsonPath("$.status", is("UPDATED")));
    }

    @Test
    void shouldDeleteBatch() throws Exception {
        mockMvc.perform(delete("/api/batches/1"))
                .andExpect(status().isNoContent());
    }
}
