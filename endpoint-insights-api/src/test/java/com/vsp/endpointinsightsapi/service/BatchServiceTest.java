package com.vsp.endpointinsightsapi.service;

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

    private TestBatchRepository testBatchRepository;
    private JobRepository jobRepository;
    private BatchService batchService;

    @BeforeEach
    void setUp() {
        testBatchRepository = Mockito.mock(TestBatchRepository.class);
        jobRepository = Mockito.mock(JobRepository.class);
        batchService = new BatchService(testBatchRepository, jobRepository);
    }

    @Test
    void getBatchById_shouldReturnBatchWhenFound() {
        UUID id = UUID.randomUUID();
        TestBatch mockBatch = new TestBatch();
        mockBatch.setBatch_id(id);
        mockBatch.setBatchName("Sample Batch");
        mockBatch.setActive(true);

        when(testBatchRepository.findById(any(UUID.class)))
                .thenReturn(Optional.of(mockBatch));

        Optional<TestBatch> result = batchService.getBatchById(id);

        assertThat(result).isPresent();
        assertThat(result.get().getBatch_id()).isEqualTo(id);
        assertThat(result.get().getBatchName()).isEqualTo("Sample Batch");
    }

    @Test
    void getBatchById_shouldReturnEmptyWhenNotFound() {
        UUID id = UUID.randomUUID();

        when(testBatchRepository.findById(any(UUID.class)))
                .thenReturn(Optional.empty());

        Optional<TestBatch> result = batchService.getBatchById(id);

        assertThat(result).isEmpty();
    }
}
