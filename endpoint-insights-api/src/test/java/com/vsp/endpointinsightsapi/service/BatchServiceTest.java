package com.vsp.endpointinsightsapi.service;

import com.vsp.endpointinsightsapi.dto.BatchResponseDTO;
import com.vsp.endpointinsightsapi.dto.BatchRequestDTO;
import com.vsp.endpointinsightsapi.exception.BatchNotFoundException;
import com.vsp.endpointinsightsapi.exception.CustomException;
import com.vsp.endpointinsightsapi.mapper.BatchMapper;
import com.vsp.endpointinsightsapi.model.entity.BatchUpdateRequest;
import com.vsp.endpointinsightsapi.model.Job;
import com.vsp.endpointinsightsapi.model.TestBatch;
import com.vsp.endpointinsightsapi.model.entity.TestBatchEmailList;
import com.vsp.endpointinsightsapi.repository.JobRepository;
import com.vsp.endpointinsightsapi.repository.TestBatchEmailListsRepository;
import com.vsp.endpointinsightsapi.repository.TestBatchRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BatchServiceTest {

    @Mock
    TestBatchRepository testBatchRepository;

    @Mock
    TestBatchEmailListsRepository testBatchEmailListsRepository;

    @Mock
    BatchSchedulerService batchSchedulerService;

    @Mock
    BatchMapper batchMapper;

    @InjectMocks
    BatchService testBatchService;

    @Mock
    JobRepository jobRepository;

    @Test
    void getBatchById_returnsDto() {
        UUID id = UUID.randomUUID();
        TestBatch entity = new TestBatch();
        entity.setBatchId(id);
        entity.setBatchName("Example");
        entity.setScheduleId(1001L);
        entity.setStartTime(LocalDate.parse("2025-11-08").atStartOfDay());
        entity.setLastTimeRun(LocalDate.parse("2025-11-09").atStartOfDay());
        entity.setActive(true);

        when(testBatchRepository.findByIdWithJobsAndUsers(id)).thenReturn(Optional.of(entity));

        BatchResponseDTO dto = BatchResponseDTO.builder()
                .id(id)
                .batchName("Example")
                .scheduleId(1001L)
                .startTime(LocalDate.parse("2025-11-08").atStartOfDay())
                .lastTimeRun(LocalDate.parse("2025-11-09").atStartOfDay())
                .active(true)
                .build();
        when(batchMapper.toDto(entity)).thenReturn(dto);

        BatchResponseDTO out = testBatchService.getBatchById(id);

        assertEquals(id, out.getId());
        assertEquals("Example", out.getBatchName());
        verify(testBatchRepository).findByIdWithJobsAndUsers(id);
        verify(batchMapper).toDto(entity);
    }

    @Test
    void getBatchById_notFound_throws() {
        UUID id = UUID.randomUUID();

        assertThrows(BatchNotFoundException.class, () -> testBatchService.getBatchById(id));
    }

    @Test
    void deleteBatchById_Exists() {
        UUID id = UUID.randomUUID();
        when(testBatchRepository.existsById(id)).thenReturn(true);

        testBatchService.deleteBatchById(id);

        verify(testBatchRepository).deleteById(id);
    }

    @Test
    void deleteBatchById_NotFound() {
        UUID id = UUID.randomUUID();
        when(testBatchRepository.existsById(id)).thenReturn(false);

        assertThrows(BatchNotFoundException.class, () -> testBatchService.deleteBatchById(id));
    }

    @Test
    void updateBatch_setJobs_success() {
        UUID batchId = UUID.randomUUID();
        UUID jobId1 = UUID.randomUUID();
        UUID jobId2 = UUID.randomUUID();

        TestBatch existingBatch = new TestBatch();
        existingBatch.setBatchId(batchId);
        existingBatch.setJobs(new ArrayList<>());

        Job job1 = new Job();
        job1.setJobId(jobId1);
        Job job2 = new Job();
        job2.setJobId(jobId2);

        BatchUpdateRequest request = new BatchUpdateRequest();
        request.setJobs(List.of(jobId1, jobId2));

        when(testBatchRepository.findById(batchId)).thenReturn(Optional.of(existingBatch));
        when(jobRepository.findAllById(List.of(jobId1, jobId2))).thenReturn(List.of(job1, job2));
        when(testBatchRepository.saveAndFlush(any(TestBatch.class))).thenReturn(existingBatch);
        when(testBatchRepository.findByIdWithJobsAndUsers(batchId)).thenReturn(Optional.of(existingBatch));

        TestBatch result = testBatchService.updateBatch(batchId, request);

        assertEquals(2, result.getJobs().size());
        assertTrue(result.getJobs().contains(job1));
        assertTrue(result.getJobs().contains(job2));
        verify(testBatchRepository).saveAndFlush(existingBatch);
    }

    @Test
    void updateBatch_replaceJobs_removesOldAddsNew() {
        UUID batchId = UUID.randomUUID();
        UUID oldJobId = UUID.randomUUID();
        UUID newJobId = UUID.randomUUID();

        Job oldJob = new Job();
        oldJob.setJobId(oldJobId);
        Job newJob = new Job();
        newJob.setJobId(newJobId);

        TestBatch existingBatch = new TestBatch();
        existingBatch.setBatchId(batchId);
        existingBatch.setJobs(new ArrayList<>(List.of(oldJob)));

        BatchUpdateRequest request = new BatchUpdateRequest();
        request.setJobs(List.of(newJobId));

        when(testBatchRepository.findById(batchId)).thenReturn(Optional.of(existingBatch));
        when(jobRepository.findAllById(List.of(newJobId))).thenReturn(List.of(newJob));
        when(testBatchRepository.saveAndFlush(any(TestBatch.class))).thenReturn(existingBatch);
        when(testBatchRepository.findByIdWithJobsAndUsers(batchId)).thenReturn(Optional.of(existingBatch));

        TestBatch result = testBatchService.updateBatch(batchId, request);

        assertEquals(1, result.getJobs().size());
        assertTrue(result.getJobs().contains(newJob));
        assertFalse(result.getJobs().contains(oldJob));
        verify(testBatchRepository).saveAndFlush(existingBatch);
    }

    @Test
    void updateBatch_clearJobs_setsEmptyList() {
        UUID batchId = UUID.randomUUID();
        UUID existingJobId = UUID.randomUUID();

        Job existingJob = new Job();
        existingJob.setJobId(existingJobId);

        TestBatch existingBatch = new TestBatch();
        existingBatch.setBatchId(batchId);
        existingBatch.setJobs(new ArrayList<>(List.of(existingJob)));

        BatchUpdateRequest request = new BatchUpdateRequest();
        request.setJobs(List.of());

        when(testBatchRepository.findById(batchId)).thenReturn(Optional.of(existingBatch));
        when(jobRepository.findAllById(List.of())).thenReturn(List.of());
        when(testBatchRepository.saveAndFlush(any(TestBatch.class))).thenReturn(existingBatch);
        when(testBatchRepository.findByIdWithJobsAndUsers(batchId)).thenReturn(Optional.of(existingBatch));

        TestBatch result = testBatchService.updateBatch(batchId, request);

        assertTrue(result.getJobs().isEmpty());
        verify(testBatchRepository).saveAndFlush(existingBatch);
    }

    @Test
    void updateBatch_jobIdNotFound_throwsException() {
        UUID batchId = UUID.randomUUID();
        UUID nonExistentJobId = UUID.randomUUID();

        TestBatch existingBatch = new TestBatch();
        existingBatch.setBatchId(batchId);
        existingBatch.setJobs(new ArrayList<>());

        BatchUpdateRequest request = new BatchUpdateRequest();
        request.setJobs(List.of(nonExistentJobId));

        when(testBatchRepository.findById(batchId)).thenReturn(Optional.of(existingBatch));
        when(jobRepository.findAllById(List.of(nonExistentJobId))).thenReturn(List.of());

        CustomException exception = assertThrows(CustomException.class,
                () -> testBatchService.updateBatch(batchId, request));
        assertTrue(exception.getErrorResponse().getDetails().contains("One or more job IDs not found"));
        verify(testBatchRepository, never()).save(any());
    }

    @Test
    void updateBatch_batchNotFound_throwsException() {
        UUID batchId = UUID.randomUUID();
        BatchUpdateRequest request = new BatchUpdateRequest();

        when(testBatchRepository.findById(batchId)).thenReturn(Optional.empty());

        CustomException exception = assertThrows(CustomException.class,
                () -> testBatchService.updateBatch(batchId, request));
        assertTrue(exception.getErrorResponse().getDetails().contains("Batch does not exist with id=" + batchId));
        verify(testBatchRepository, never()).save(any());
    }

    @Test
    void updateBatch_setCronExpression_success() {
        UUID batchId = UUID.randomUUID();

        TestBatch existingBatch = new TestBatch();
        existingBatch.setBatchId(batchId);
        existingBatch.setJobs(new ArrayList<>());

        BatchUpdateRequest request = new BatchUpdateRequest();
        request.setCronExpression("0 30 14 * * MON,WED,FRI");

        when(testBatchRepository.findById(batchId)).thenReturn(Optional.of(existingBatch));
        when(testBatchRepository.saveAndFlush(any(TestBatch.class))).thenReturn(existingBatch);
        when(testBatchRepository.findByIdWithJobsAndUsers(batchId)).thenReturn(Optional.of(existingBatch));

        TestBatch result = testBatchService.updateBatch(batchId, request);

        assertEquals("0 30 14 * * MON,WED,FRI", result.getCronExpression());
        verify(testBatchRepository).saveAndFlush(existingBatch);
    }

    @Test
    void updateBatch_nullCronExpression_doesNotOverwrite() {
        UUID batchId = UUID.randomUUID();

        TestBatch existingBatch = new TestBatch();
        existingBatch.setBatchId(batchId);
        existingBatch.setCronExpression("0 0 12 * * *");
        existingBatch.setJobs(new ArrayList<>());

        BatchUpdateRequest request = new BatchUpdateRequest();
        request.setCronExpression(null);

        when(testBatchRepository.findById(batchId)).thenReturn(Optional.of(existingBatch));
        when(testBatchRepository.saveAndFlush(any(TestBatch.class))).thenReturn(existingBatch);
        when(testBatchRepository.findByIdWithJobsAndUsers(batchId)).thenReturn(Optional.of(existingBatch));

        TestBatch result = testBatchService.updateBatch(batchId, request);

        assertEquals("0 0 12 * * *", result.getCronExpression());
        verify(testBatchRepository).saveAndFlush(existingBatch);
    }

    @Test
    void updateBatch_withEmails_updatesEmailList() {
        UUID batchId = UUID.randomUUID();

        TestBatch existingBatch = new TestBatch();
        existingBatch.setBatchId(batchId);
        existingBatch.setJobs(new ArrayList<>());

        BatchUpdateRequest request = new BatchUpdateRequest();
        request.setEmails(List.of("a@example.com", "b@example.com"));

        when(testBatchRepository.findById(batchId)).thenReturn(Optional.of(existingBatch));
        when(testBatchRepository.saveAndFlush(any(TestBatch.class))).thenReturn(existingBatch);
        when(testBatchRepository.findByIdWithJobsAndUsers(batchId)).thenReturn(Optional.of(existingBatch));

        testBatchService.updateBatch(batchId, request);

        verify(testBatchEmailListsRepository).deleteAllByBatchId(batchId);
        verify(testBatchEmailListsRepository).saveAll(argThat(iterable -> {
            List<TestBatchEmailList> list = (List<TestBatchEmailList>) iterable;
            return list.size() == 2 &&
                    list.stream().allMatch(item -> item.getBatchId().equals(batchId));
        }));
    }

    @Test
    void updateBatch_withDuplicateEmails_deduplicates() {
        UUID batchId = UUID.randomUUID();

        TestBatch existingBatch = new TestBatch();
        existingBatch.setBatchId(batchId);
        existingBatch.setJobs(new ArrayList<>());

        BatchUpdateRequest request = new BatchUpdateRequest();
        request.setEmails(List.of("dup@example.com", "DUP@example.com", "dup@example.com"));

        when(testBatchRepository.findById(batchId)).thenReturn(Optional.of(existingBatch));
        when(testBatchRepository.saveAndFlush(any(TestBatch.class))).thenReturn(existingBatch);
        when(testBatchRepository.findByIdWithJobsAndUsers(batchId)).thenReturn(Optional.of(existingBatch));

        testBatchService.updateBatch(batchId, request);

        verify(testBatchEmailListsRepository).saveAll(argThat(iterable -> {
            List<TestBatchEmailList> list = (List<TestBatchEmailList>) iterable;
            return list.size() == 1;
        }));
    }

    @Test
    void updateBatch_nullEmails_doesNotUpdateEmailList() {
        UUID batchId = UUID.randomUUID();

        TestBatch existingBatch = new TestBatch();
        existingBatch.setBatchId(batchId);
        existingBatch.setJobs(new ArrayList<>());

        BatchUpdateRequest request = new BatchUpdateRequest();
        request.setEmails(null);

        when(testBatchRepository.findById(batchId)).thenReturn(Optional.of(existingBatch));
        when(testBatchRepository.saveAndFlush(any(TestBatch.class))).thenReturn(existingBatch);
        when(testBatchRepository.findByIdWithJobsAndUsers(batchId)).thenReturn(Optional.of(existingBatch));

        testBatchService.updateBatch(batchId, request);

        verify(testBatchEmailListsRepository, never()).deleteAllByBatchId(any());
        verify(testBatchEmailListsRepository, never()).saveAll(any());
    }

    @Test
    void updateBatch_emptyRequest_noChanges() {
        UUID batchId = UUID.randomUUID();

        TestBatch existingBatch = new TestBatch();
        existingBatch.setBatchId(batchId);
        existingBatch.setJobs(new ArrayList<>());

        BatchUpdateRequest request = new BatchUpdateRequest();

        when(testBatchRepository.findById(batchId)).thenReturn(Optional.of(existingBatch));
        when(testBatchRepository.saveAndFlush(any(TestBatch.class))).thenReturn(existingBatch);
        when(testBatchRepository.findByIdWithJobsAndUsers(batchId)).thenReturn(Optional.of(existingBatch));

        TestBatch result = testBatchService.updateBatch(batchId, request);

        assertEquals(0, result.getJobs().size());
        verify(testBatchRepository).saveAndFlush(existingBatch);
        verify(testBatchEmailListsRepository, never()).deleteAllByBatchId(any());
    }

    @Test
    void get_all_match_by_criteria() {
        UUID batchId = UUID.randomUUID();

        TestBatch batch = new TestBatch();
        batch.setBatchId(batchId);
        batch.setBatchName("Test Batch");

        BatchResponseDTO dto = new BatchResponseDTO();
        dto.setId(batchId);
        dto.setBatchName("Test Batch");

        when(testBatchRepository.findAllByCriteria("", null)).thenReturn(List.of(batch));
        when(batchMapper.toDto(batch)).thenReturn(dto);
        List<BatchResponseDTO> result = testBatchService.getAllBatchesByCriteria("", null);

        assertEquals(1, result.size());
        assertEquals("Test Batch", result.get(0).getBatchName());
    }

    @Test
    void createBatch_withEmails_savesEmails() {
        UUID batchId = UUID.randomUUID();
        TestBatch batch = new TestBatch();
        batch.setBatchId(batchId);
        batch.setBatchName("Test Batch");
        batch.setJobs(new ArrayList<>());

        BatchRequestDTO request = new BatchRequestDTO();
        request.setBatchName("Test Batch");
        request.setJobs(new ArrayList<>());
        request.setEmails(List.of("test@example.com"));
        request.setGroupIds(new ArrayList<>());

        when(testBatchRepository.saveAndFlush(any(TestBatch.class))).thenReturn(batch);
        when(testBatchRepository.findByIdWithJobsAndUsers(batchId)).thenReturn(Optional.of(batch));

        testBatchService.createBatch(request);

        verify(testBatchRepository).saveAndFlush(any(TestBatch.class));
        verify(testBatchEmailListsRepository).saveAll(any());
    }

    @Test
    void createBatch_withGroupIds_savesGroupIdentifiers() {
        UUID batchId = UUID.randomUUID();
        UUID groupId = UUID.randomUUID();
        TestBatch batch = new TestBatch();
        batch.setBatchId(batchId);
        batch.setBatchName("Test Batch");
        batch.setJobs(new ArrayList<>());

        BatchRequestDTO request = new BatchRequestDTO();
        request.setBatchName("Test Batch");
        request.setJobs(new ArrayList<>());
        request.setEmails(new ArrayList<>());
        request.setGroupIds(List.of(groupId));

        when(testBatchRepository.saveAndFlush(any(TestBatch.class))).thenReturn(batch);
        when(testBatchRepository.findByIdWithJobsAndUsers(batchId)).thenReturn(Optional.of(batch));

        testBatchService.createBatch(request);

        verify(testBatchRepository).saveAndFlush(any(TestBatch.class));
        verify(testBatchEmailListsRepository).saveAll(any());
    }

    @Test
    void createBatch_withEmailsAndGroupIds_savesBoth() {
        UUID batchId = UUID.randomUUID();
        UUID groupId = UUID.randomUUID();
        TestBatch batch = new TestBatch();
        batch.setBatchId(batchId);
        batch.setBatchName("Test Batch");
        batch.setJobs(new ArrayList<>());

        BatchRequestDTO request = new BatchRequestDTO();
        request.setBatchName("Test Batch");
        request.setJobs(new ArrayList<>());
        request.setEmails(List.of("test@example.com"));
        request.setGroupIds(List.of(groupId));

        when(testBatchRepository.saveAndFlush(any(TestBatch.class))).thenReturn(batch);
        when(testBatchRepository.findByIdWithJobsAndUsers(batchId)).thenReturn(Optional.of(batch));

        testBatchService.createBatch(request);

        verify(testBatchRepository).saveAndFlush(any(TestBatch.class));
        verify(testBatchEmailListsRepository).saveAll(any());
    }

    @Test
    void updateBatch_withGroupIds_savesGroupIdentifiers() {
        UUID batchId = UUID.randomUUID();
        UUID groupId = UUID.randomUUID();

        TestBatch existingBatch = new TestBatch();
        existingBatch.setBatchId(batchId);
        existingBatch.setJobs(new ArrayList<>());

        BatchUpdateRequest request = new BatchUpdateRequest();
        request.setEmails(null);
        request.setGroupIds(List.of(groupId));

        when(testBatchRepository.findById(batchId)).thenReturn(Optional.of(existingBatch));
        when(testBatchRepository.saveAndFlush(any(TestBatch.class))).thenReturn(existingBatch);
        when(testBatchRepository.findByIdWithJobsAndUsers(batchId)).thenReturn(Optional.of(existingBatch));

        testBatchService.updateBatch(batchId, request);

        verify(testBatchEmailListsRepository).deleteAllByBatchId(batchId);
        verify(testBatchEmailListsRepository).saveAll(any());
    }

    @Test
    void updateEmailsForBatch_backwardCompatibilityOverload_callsMainMethod() {
        UUID batchId = UUID.randomUUID();
        List<String> emails = List.of("test@example.com");

        testBatchService.updateEmailsForBatch(batchId, emails);

        verify(testBatchEmailListsRepository).deleteAllByBatchId(batchId);
        verify(testBatchEmailListsRepository).saveAll(any());
    }
}
