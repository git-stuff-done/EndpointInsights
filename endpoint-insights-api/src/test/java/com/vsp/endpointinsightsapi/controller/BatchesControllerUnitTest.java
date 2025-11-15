package com.vsp.endpointinsightsapi.controller;

import com.vsp.endpointinsightsapi.dto.BatchRequestDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BatchesController.class)
@AutoConfigureWebMvc
public class BatchesControllerUnitTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getBatches_returnsListOfBatches() throws Exception {
        mockMvc.perform(get("/api/batches"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Daily API Tests"))
                .andExpect(jsonPath("$[1].status").value("INACTIVE"));
    }

    @Test
    void getBatchById_returnsSingleBatch() throws Exception {
        mockMvc.perform(get("/api/batches/{id}", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Example Batch 1"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void createBatch_returnsCreatedBatch() throws Exception {
        BatchRequestDTO request = new BatchRequestDTO();
        request.setName("Nightly Test Run");

        mockMvc.perform(post("/api/batches")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Nightly Test Run"))
                .andExpect(jsonPath("$.status").value("CREATED"));
    }

    @Test
    void updateBatch_returnsUpdatedBatch() throws Exception {
        BatchRequestDTO request = new BatchRequestDTO();
        request.setName("Updated Batch");

        mockMvc.perform(put("/api/batches/{id}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Batch"))
                .andExpect(jsonPath("$.status").value("UPDATED"));
    }

    @Test
    void deleteBatch_returnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/batches/{id}", 1))
                .andExpect(status().isOk())
                .andExpect(content().string("Batch 1 deleted"));
    }
}
