package com.vsp.endpointinsightsapi.service;

import com.vsp.endpointinsightsapi.dto.BatchRequestDTO;
import com.vsp.endpointinsightsapi.model.TestBatch;
import com.vsp.endpointinsightsapi.repository.JobRepository;
import com.vsp.endpointinsightsapi.repository.TestBatchRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class BatchServiceTest {

    private TestBatchRepository batchRepository;
    private BatchService batchService;
    private JobRepository jobRepository;

    @BeforeEach
    void setUp() {
        batchRepository = Mockito.mock(TestBatchRepository.class);
        jobRepository = Mockito.mock(JobRepository.class);
        batchService = new BatchService(batchRepository, jobRepository);
    }

    @Test
    void createBatch_ReturnSavedBatch() {
        BatchRequestDTO batch = new BatchRequestDTO();
        when(batchRepository.save(any(TestBatch.class))).thenReturn(new TestBatch());
        TestBatch testResult = batchService.createBatch(batch);
        assertThat(testResult).isNotNull();
        Mockito.verify(batchRepository, Mockito.times(1)).save(any(TestBatch.class));
    }

}