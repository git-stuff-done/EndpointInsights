package com.vsp.endpointinsightsapi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vsp.endpointinsightsapi.dto.BatchRequestDTO;
import com.vsp.endpointinsightsapi.service.BatchService;
import com.vsp.endpointinsightsapi.controller.BatchesController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.ActiveProfiles;


import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.UUID;

@TestPropertySource(properties = "app.authentication.enabled=false")
@WebMvcTest(BatchesController.class)
@ActiveProfiles("test")
class BatchesControllerUnitTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @TestConfiguration
    static class TestConfig {
        @Bean
        public ObjectMapper objectMapper() {
            return new ObjectMapper();
        }
    }

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
        mockMvc.perform(get("/api/batches/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", containsString("Example Batch")))
                .andExpect(jsonPath("$.status", is("ACTIVE")));
    }

    @Test
    void shouldCreateBatch() throws Exception {
        BatchRequestDTO request = new BatchRequestDTO("New Batch", null);
        mockMvc.perform(post("/api/batches")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is("New Batch")))
                .andExpect(jsonPath("$.status", is("CREATED")));
    }

    @Test
    void shouldUpdateBatch() throws Exception {
        BatchRequestDTO request = new BatchRequestDTO("Updated Batch", null);

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
