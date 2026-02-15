package com.vsp.endpointinsightsapi.controller;

import com.vsp.endpointinsightsapi.mapper.BatchMapper;
import org.junit.jupiter.api.BeforeEach;
import org.mapstruct.factory.Mappers;
import tools.jackson.databind.ObjectMapper;
import com.vsp.endpointinsightsapi.dto.BatchRequestDTO;
import com.vsp.endpointinsightsapi.dto.BatchResponseDTO;
import com.vsp.endpointinsightsapi.exception.BatchNotFoundException;
import com.vsp.endpointinsightsapi.model.entity.BatchUpdateRequest;
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
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.*;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TestPropertySource(properties = "app.authentication.enabled=false")
@WebMvcTest(controllers = BatchesController.class)
class BatchesControllerUnitTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    private BatchMapper batchMapper;

    @MockitoBean
    private BatchService batchService;

    @BeforeEach
    void setUp() {
        batchMapper = Mappers.getMapper(BatchMapper.class);
    }


    @Test
    void shouldReturnListOfBatches() throws Exception {
        TestBatch batch = new TestBatch();
        batch.setBatch_id(UUID.randomUUID());
        batch.setBatchName("Test Batch");
        batch.setActive(true);

        BatchResponseDTO batchDTO = batchMapper.toDto(batch);
        when(batchService.getAllBatchesByCriteria("", null)).thenReturn(List.of(batchDTO));

        mockMvc.perform(get("/api/batches"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturnBatchById() throws Exception {
        UUID id = UUID.randomUUID();

        BatchResponseDTO dto = BatchResponseDTO.builder()
                .id(id)
                .batchName("Daily API Tests")
                .scheduleId(1001L)
                .startTime(LocalDate.parse("2025-11-08").atStartOfDay())
                .lastTimeRun(LocalDate.parse("2025-11-09").atStartOfDay())
                .active(true)
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
        BatchRequestDTO request = new BatchRequestDTO();
        request.setBatchName("New Batch");
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
    void shouldNotUpdateBatchWithBadId() throws Exception {
        BatchUpdateRequest request = new BatchUpdateRequest();

        mockMvc.perform(put("/api/batches/{id}", "bad-uuid")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.error", containsString("Invalid Parameter Type")));
    }

    @Test
    void shouldDeleteBatch() throws Exception {
        UUID id = UUID.randomUUID();

        org.mockito.Mockito.doNothing()
                .when(batchService).deleteBatchById(id);

        mockMvc.perform(delete("/api/batches/{id}", id))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldReturn404WhenDeletingNonexistentBatch() throws Exception {
        UUID id = UUID.randomUUID();

        org.mockito.Mockito.doThrow(new BatchNotFoundException(id.toString()))
                .when(batchService).deleteBatchById(id);

        mockMvc.perform(delete("/api/batches/{id}", id))
                .andExpect(status().isNotFound());
    }


    @Test
    void updateBatch_ShouldReturnUpdatedBatch() throws Exception {
        UUID batchId = UUID.randomUUID();
        BatchUpdateRequest request = new BatchUpdateRequest();
        TestBatch updatedBatch = new TestBatch();
        updatedBatch.setBatch_id(batchId);

        mockMvc.perform(put("/api/batches/{id}", batchId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

}
