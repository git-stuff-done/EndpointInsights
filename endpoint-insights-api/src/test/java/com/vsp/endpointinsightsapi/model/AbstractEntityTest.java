package com.vsp.endpointinsightsapi.model;

import com.vsp.endpointinsightsapi.repository.TestBatchRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AbstractEntityTest {

    @Mock
    private TestBatchRepository testBatchRepository;

    @Test
    public void createEntityWithAuditFields() {
        // Arrange
        TestBatch batch = new TestBatch();
        batch.setBatchName("batchName");
        batch.setJobs(new ArrayList<>());

        // Create a mock saved entity with ID
        TestBatch savedBatch = new TestBatch();
        UUID batchId = UUID.randomUUID();
        savedBatch.setBatch_id(batchId);
        savedBatch.setBatchName("batchName");

        when(testBatchRepository.save(any(TestBatch.class))).thenReturn(savedBatch);

        when(testBatchRepository.findById(batchId)).thenReturn(savedBatch);

        TestBatch result = testBatchRepository.save(batch);
        TestBatch expectedBatch = testBatchRepository.findById(savedBatch.getBatch_id());

        assertNotNull(result);
        assertNotNull(expectedBatch);
        assertEquals("batchName", expectedBatch.getBatchName());
        assertEquals(batchId, expectedBatch.getBatch_id());

        verify(testBatchRepository, times(1)).save(batch);
        verify(testBatchRepository, times(1)).findById(batchId);
    }
}