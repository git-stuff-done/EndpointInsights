package com.vsp.endpointinsightsapi.controller;

import tools.jackson.databind.ObjectMapper;
import com.vsp.endpointinsightsapi.dto.BatchRequestDTO;
import com.vsp.endpointinsightsapi.model.TestBatch;
import com.vsp.endpointinsightsapi.service.BatchService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


//Unit tests for BatchesController.
@WebMvcTest(controllers = BatchesController.class)
@TestPropertySource(properties = "app.authentication.enabled=false")
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
                .andExpect(jsonPath("$[0].name", not(emptyString())))
                .andExpect(jsonPath("$[0].status", not(emptyString())));
    }

    @Test
    void shouldReturnBatchById() throws Exception {
        UUID id = UUID.randomUUID();
        TestBatch batch = new TestBatch(id, null, "Example Batch", 1L, LocalDate.now(), LocalDate.now(), true);

        when(batchService.getBatchById(any(UUID.class)))
                .thenReturn(Optional.of(batch));

        mockMvc.perform(get("/api/batches/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.batchName", is("Example Batch")))
                .andExpect(jsonPath("$.active", is(true)));
    }

    @Test
    void shouldReturnNotFoundWhenBatchMissing() throws Exception {
        UUID id = UUID.randomUUID();

        when(batchService.getBatchById(any(UUID.class)))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/api/batches/" + id))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldCreateBatch() throws Exception {
        BatchRequestDTO request = new BatchRequestDTO("New Batch", "Description");

        mockMvc.perform(post("/api/batches")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(99)))
                .andExpect(jsonPath("$.name", is("New Batch")))
                .andExpect(jsonPath("$.status", is("CREATED")));
    }

    @Test
    void shouldUpdateBatch() throws Exception {
        BatchRequestDTO request = new BatchRequestDTO("Updated Batch", "Updated Description");

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
