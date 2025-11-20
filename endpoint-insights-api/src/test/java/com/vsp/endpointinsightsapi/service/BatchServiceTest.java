// BatchServiceTest.java
package com.vsp.endpointinsightsapi.service;

import com.vsp.endpointinsightsapi.dto.BatchResponseDTO;
import com.vsp.endpointinsightsapi.exception.BatchNotFoundException;
import com.vsp.endpointinsightsapi.mapper.BatchMapper;
import com.vsp.endpointinsightsapi.model.TestBatch;
import com.vsp.endpointinsightsapi.repository.JobRepository;
import com.vsp.endpointinsightsapi.repository.TestBatchRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BatchServiceTest {

    @Mock TestBatchRepository testBatchRepository;
    @Mock BatchMapper batchMapper;
    @Mock BatchService batchService;
    @Mock JobRepository jobRepository;

    @Test
    void getBatchById_returnsDto() {
        UUID id = UUID.randomUUID();
        TestBatch entity = new TestBatch();
        // adjust getters/setters to your model
        entity.setBatch_id(id);
        entity.setBatchName("Example");
        entity.setScheduleId(1001L);
        entity.setStartTime(LocalDate.parse("2025-11-08"));
        entity.setLastTimeRun(LocalDate.parse("2025-11-09"));
        entity.setActive(true);

        when(testBatchRepository.findById(id)).thenReturn(Optional.of(entity));

        BatchResponseDTO dto = BatchResponseDTO.builder()
                .id(id)
                .batchName("Example")
                .scheduleId(1001L)
                .startTime(LocalDate.parse("2025-11-08"))
                .lastTimeRun(LocalDate.parse("2025-11-09"))
                .active(true)
                .build();
        when(batchMapper.toDto(entity)).thenReturn(dto);

        BatchResponseDTO out = batchService.getBatchById(id);

        assertEquals(id, out.getId());
        assertEquals("Example", out.getBatchName());
        verify(testBatchRepository).findById(id);
        verify(batchMapper).toDto(entity);
    }

    @Test
    void getBatchById_notFound_throws() {
        UUID id = UUID.randomUUID();
        when(testBatchRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(BatchNotFoundException.class, () -> batchService.getBatchById(id));
    }

    @Test
    void deleteBatchById_Exists() {
        UUID id = UUID.randomUUID();
        when(testBatchRepository.existsById(id)).thenReturn(true);

        batchService.deleteBatchById(id);

        verify(testBatchRepository).deleteById(id);
    }

    @Test
    void deleteBatchById_NotFound() {
        UUID id = UUID.randomUUID();
        when(testBatchRepository.existsById(id)).thenReturn(false);

        assertThrows(BatchNotFoundException.class, () -> batchService.deleteBatchById(id));
    }
}
