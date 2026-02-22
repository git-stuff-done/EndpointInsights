// BatchServiceTest.java
package com.vsp.endpointinsightsapi.service;

import com.vsp.endpointinsightsapi.dto.BatchRequestDTO;
import com.vsp.endpointinsightsapi.dto.BatchResponseDTO;
import com.vsp.endpointinsightsapi.exception.BatchNotFoundException;
import com.vsp.endpointinsightsapi.mapper.BatchMapper;
import com.vsp.endpointinsightsapi.model.Job;
import com.vsp.endpointinsightsapi.model.TestBatch;
import com.vsp.endpointinsightsapi.repository.JobRepository;
import com.vsp.endpointinsightsapi.repository.TestBatchRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BatchServiceTest {

    @Mock TestBatchRepository testBatchRepository;
    @Mock BatchMapper batchMapper;
    @InjectMocks BatchService batchService;
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

    @Test
    void createBatch_withNoJobs_createsBatch() {
        BatchRequestDTO request = new BatchRequestDTO();
        request.setName("Test Batch");
        request.setJobIds(Collections.emptyList());

        TestBatch savedBatch = new TestBatch();
        savedBatch.setBatch_id(UUID.randomUUID());
        savedBatch.setBatchName("Test Batch");

        when(testBatchRepository.save(any(TestBatch.class))).thenReturn(savedBatch);

        TestBatch result = batchService.createBatch(request);

        assertNotNull(result);
        assertEquals("Test Batch", result.getBatchName());
        verify(testBatchRepository).save(any(TestBatch.class));
    }

    @Test
    void createBatch_withJobs_createsBatchWithJobs() {
        UUID jobId1 = UUID.randomUUID();
        UUID jobId2 = UUID.randomUUID();
        
        BatchRequestDTO request = new BatchRequestDTO();
        request.setName("Test Batch");
        request.setJobIds(Arrays.asList(jobId1, jobId2));

        Job job1 = new Job();
        job1.setJobId(jobId1);
        Job job2 = new Job();
        job2.setJobId(jobId2);
        List<Job> jobs = Arrays.asList(job1, job2);

        when(jobRepository.findAllById(request.getJobIds())).thenReturn(jobs);

        TestBatch savedBatch = new TestBatch();
        savedBatch.setBatch_id(UUID.randomUUID());
        savedBatch.setBatchName("Test Batch");
        savedBatch.setJobs(jobs);

        when(testBatchRepository.save(any(TestBatch.class))).thenReturn(savedBatch);

        TestBatch result = batchService.createBatch(request);

        assertNotNull(result);
        assertEquals("Test Batch", result.getBatchName());
        assertEquals(2, result.getJobs().size());
        verify(jobRepository).findAllById(request.getJobIds());
        verify(testBatchRepository).save(any(TestBatch.class));
    }

    @Test
    void createBatch_withNullJobIds_createsBatch() {
        BatchRequestDTO request = new BatchRequestDTO();
        request.setName("Test Batch");
        request.setJobIds(null);

        TestBatch savedBatch = new TestBatch();
        savedBatch.setBatch_id(UUID.randomUUID());
        savedBatch.setBatchName("Test Batch");

        when(testBatchRepository.save(any(TestBatch.class))).thenReturn(savedBatch);

        TestBatch result = batchService.createBatch(request);

        assertNotNull(result);
        assertEquals("Test Batch", result.getBatchName());
        verify(testBatchRepository).save(any(TestBatch.class));
    }

    @Test
    void constructor_withNullRepository_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> 
            new BatchService(null, batchMapper, jobRepository));
    }

    @Test
    void constructor_withNullMapper_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> 
            new BatchService(testBatchRepository, null, jobRepository));
    }

    @Test
    void constructor_withNullJobRepository_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> 
            new BatchService(testBatchRepository, batchMapper, null));
    }
}
