// BatchServiceTest.java
package com.vsp.endpointinsightsapi.service;

import com.vsp.endpointinsightsapi.dto.BatchResponseDTO;
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
import org.mockito.stubbing.OngoingStubbing;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BatchServiceTest {

    @Mock
    TestBatchRepository testBatchRepository;

    @Mock
    TestBatchEmailListsRepository testBatchEmailListsRepository;

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
        // adjust getters/setters to your model
        entity.setBatchId(id);
        entity.setBatchName("Example");
        entity.setScheduleId(1001L);
        entity.setStartTime(LocalDate.parse("2025-11-08").atStartOfDay());
        entity.setLastTimeRun(LocalDate.parse("2025-11-09").atStartOfDay());
        entity.setActive(true);

        when(testBatchRepository.findById(id)).thenReturn(Optional.of(entity));

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
        verify(testBatchRepository).findById(id);
        verify(batchMapper).toDto(entity);
    }

    @Test
    void getBatchById_notFound_throws() {
        UUID id = UUID.randomUUID();
        when(testBatchRepository.findById(id)).thenReturn(Optional.empty());

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
    void updateBatch_addJobs_success() {
        // Arrange
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
        request.addJobs = List.of(jobId1, jobId2);

        when(testBatchRepository.findById(batchId)).thenReturn(Optional.of(existingBatch));
        when(jobRepository.findAllById(List.of(jobId1, jobId2))).thenReturn(List.of(job1, job2));
        when(testBatchRepository.save(any(TestBatch.class))).thenReturn(existingBatch);

        // Act
        TestBatch result = testBatchService.updateBatch(batchId, request);

        // Assert
        assertEquals(2, result.getJobs().size());
        assertTrue(result.getJobs().contains(job1));
        assertTrue(result.getJobs().contains(job2));
        verify(testBatchRepository).save(existingBatch);
    }

    @Test
    void updateBatch_removeJobs_success() {
        // Arrange
        UUID batchId = UUID.randomUUID();
        UUID jobId1 = UUID.randomUUID();
        UUID jobId2 = UUID.randomUUID();

        Job job1 = new Job();
        job1.setJobId(jobId1);
        Job job2 = new Job();
        job2.setJobId(jobId2);

        TestBatch existingBatch = new TestBatch();
        existingBatch.setBatchId(batchId);
        existingBatch.setJobs(new ArrayList<>(List.of(job1, job2)));

        BatchUpdateRequest request = new BatchUpdateRequest();
        request.deleteJobs = List.of(jobId1);

        when(testBatchRepository.findById(batchId)).thenReturn(Optional.of(existingBatch));
        when(jobRepository.findAllById(List.of(jobId1))).thenReturn(List.of(job1));
        when(testBatchRepository.save(any(TestBatch.class))).thenReturn(existingBatch);

        // Act
        TestBatch result = testBatchService.updateBatch(batchId, request);

        // Assert
        assertEquals(1, result.getJobs().size());
        assertFalse(result.getJobs().contains(job1));
        assertTrue(result.getJobs().contains(job2));
        verify(testBatchRepository).save(existingBatch);
    }

    @Test
    void updateBatch_addAndRemoveJobs_success() {
        // Arrange
        UUID batchId = UUID.randomUUID();
        UUID existingJobId = UUID.randomUUID();
        UUID newJobId = UUID.randomUUID();
        UUID jobToRemoveId = UUID.randomUUID();

        Job existingJob = new Job();
        existingJob.setJobId(existingJobId);
        Job newJob = new Job();
        newJob.setJobId(newJobId);
        Job jobToRemove = new Job();
        jobToRemove.setJobId(jobToRemoveId);

        TestBatch existingBatch = new TestBatch();
        existingBatch.setBatchId(batchId);
        existingBatch.setJobs(new ArrayList<>(List.of(existingJob, jobToRemove)));

        BatchUpdateRequest request = new BatchUpdateRequest();
        request.addJobs = List.of(newJobId);
        request.deleteJobs = List.of(jobToRemoveId);

        when(testBatchRepository.findById(batchId)).thenReturn(Optional.of(existingBatch));
        when(jobRepository.findAllById(List.of(newJobId))).thenReturn(List.of(newJob));
        when(jobRepository.findAllById(List.of(jobToRemoveId))).thenReturn(List.of(jobToRemove));
        when(testBatchRepository.save(any(TestBatch.class))).thenReturn(existingBatch);

        // Act
        TestBatch result = testBatchService.updateBatch(batchId, request);

        // Assert
        assertEquals(2, result.getJobs().size());
        assertTrue(result.getJobs().contains(existingJob));
        assertTrue(result.getJobs().contains(newJob));
        assertFalse(result.getJobs().contains(jobToRemove));
        verify(testBatchRepository).save(existingBatch);
    }

    @Test
    void updateBatch_batchNotFound_throwsException() {
        // Arrange
        UUID batchId = UUID.randomUUID();
        BatchUpdateRequest request = new BatchUpdateRequest();

        when(testBatchRepository.findById(batchId)).thenReturn(Optional.empty());

        // Act & Assert
        CustomException exception = assertThrows(CustomException.class,
            () -> testBatchService.updateBatch(batchId, request));
        assertTrue(exception.getErrorResponse().getDetails().contains("Batch does not exist with id=" + batchId));
        verify(testBatchRepository, never()).save(any());
    }

    @Test
    void updateBatch_addNonExistentJob_throwsException() {
        // Arrange
        UUID batchId = UUID.randomUUID();
        UUID nonExistentJobId = UUID.randomUUID();

        TestBatch existingBatch = new TestBatch();
        existingBatch.setBatchId(batchId);
        existingBatch.setJobs(new ArrayList<>());

        BatchUpdateRequest request = new BatchUpdateRequest();
        request.addJobs = List.of(nonExistentJobId);

        when(testBatchRepository.findById(batchId)).thenReturn(Optional.of(existingBatch));
        when(jobRepository.findAllById(List.of(nonExistentJobId))).thenReturn(List.of());

        // Act & Assert
        CustomException exception = assertThrows(CustomException.class,
            () -> testBatchService.updateBatch(batchId, request));
        assertTrue(exception.getErrorResponse().getDetails().contains("Job does not exist with id=" + nonExistentJobId));
        verify(testBatchRepository, never()).save(any());
    }

    @Test
    void updateBatch_addJobAlreadyInBatch_throwsException() {
        // Arrange
        UUID batchId = UUID.randomUUID();
        UUID existingJobId = UUID.randomUUID();

        Job existingJob = new Job();
        existingJob.setJobId(existingJobId);

        TestBatch existingBatch = new TestBatch();
        existingBatch.setBatchId(batchId);
        existingBatch.setJobs(new ArrayList<>(List.of(existingJob)));

        BatchUpdateRequest request = new BatchUpdateRequest();
        request.addJobs = List.of(existingJobId);

        when(testBatchRepository.findById(batchId)).thenReturn(Optional.of(existingBatch));
        when(jobRepository.findAllById(List.of(existingJobId))).thenReturn(List.of(existingJob));

        // Act & Assert
        CustomException exception = assertThrows(CustomException.class,
            () -> testBatchService.updateBatch(batchId, request));
        assertTrue(exception.getErrorResponse().getDetails().contains("Job already exists in batch with id=" + batchId));
        verify(testBatchRepository, never()).save(any());
    }

    @Test
    void updateBatch_removeNonExistentJob_throwsException() {
        // Arrange
        UUID batchId = UUID.randomUUID();
        UUID nonExistentJobId = UUID.randomUUID();

        TestBatch existingBatch = new TestBatch();
        existingBatch.setBatchId(batchId);
        existingBatch.setJobs(new ArrayList<>());

        BatchUpdateRequest request = new BatchUpdateRequest();
        request.deleteJobs = List.of(nonExistentJobId);

        when(testBatchRepository.findById(batchId)).thenReturn(Optional.of(existingBatch));
        when(jobRepository.findAllById(List.of(nonExistentJobId))).thenReturn(List.of());

        // Act & Assert
        CustomException exception = assertThrows(CustomException.class,
            () -> testBatchService.updateBatch(batchId, request));
        assertTrue(exception.getErrorResponse().getDetails().contains("Job does not exist with id=" + nonExistentJobId));
        verify(testBatchRepository, never()).save(any());
    }

    @Test
    void updateBatch_removeJobNotInBatch_throwsException() {
        // Arrange
        UUID batchId = UUID.randomUUID();
        UUID jobNotInBatchId = UUID.randomUUID();

        Job jobNotInBatch = new Job();
        jobNotInBatch.setJobId(jobNotInBatchId);

        TestBatch existingBatch = new TestBatch();
        existingBatch.setBatchId(batchId);
        existingBatch.setJobs(new ArrayList<>());

        BatchUpdateRequest request = new BatchUpdateRequest();
        request.deleteJobs = List.of(jobNotInBatchId);

        when(testBatchRepository.findById(batchId)).thenReturn(Optional.of(existingBatch));
        when(jobRepository.findAllById(List.of(jobNotInBatchId))).thenReturn(List.of(jobNotInBatch));

        // Act & Assert
        CustomException exception = assertThrows(CustomException.class,
            () -> testBatchService.updateBatch(batchId, request));
        assertTrue(exception.getErrorResponse().getDetails().contains("Job does not exist in batch with id=" + batchId));
        verify(testBatchRepository, never()).save(any());
    }

    @Test
    void updateBatch_emptyRequest_noChanges() {
        // Arrange
        UUID batchId = UUID.randomUUID();

        TestBatch existingBatch = new TestBatch();
        existingBatch.setBatchId(batchId);
        existingBatch.setJobs(new ArrayList<>());

        BatchUpdateRequest request = new BatchUpdateRequest();
        // No add or delete jobs specified

        OngoingStubbing<Optional<TestBatch>> optionalOngoingStubbing = when(testBatchRepository.findById(batchId)).thenReturn(Optional.of(existingBatch));
        when(testBatchRepository.save(any(TestBatch.class))).thenReturn(existingBatch);

        // Act
        TestBatch result = testBatchService.updateBatch(batchId, request);

        // Assert
        assertEquals(0, result.getJobs().size());
        verify(testBatchRepository).save(existingBatch);
    }

    @Test
    void updateBatch_nullAddJobsList_handledGracefully() {
        // Arrange
        UUID batchId = UUID.randomUUID();
        UUID jobId = UUID.randomUUID();

        Job job = new Job();
        job.setJobId(jobId);

        TestBatch existingBatch = new TestBatch();
        existingBatch.setBatchId(batchId);
        existingBatch.setJobs(new ArrayList<>(List.of(job)));

        BatchUpdateRequest request = new BatchUpdateRequest();
        request.addJobs = null; // Null list
        request.deleteJobs = List.of(jobId);

        when(testBatchRepository.findById(batchId)).thenReturn(Optional.of(existingBatch));
        when(jobRepository.findAllById(List.of(jobId))).thenReturn(List.of(job));
        when(testBatchRepository.save(any(TestBatch.class))).thenReturn(existingBatch);

        // Act
        TestBatch result = testBatchService.updateBatch(batchId, request);

        // Assert
        assertEquals(0, result.getJobs().size());
        verify(testBatchRepository).save(existingBatch);
    }


    @Test
    void get_all_match_by_criteria(){
        UUID batchId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        TestBatch batch = new TestBatch();
        batch.setBatchId(batchId);
        batch.setBatchName("Test Batch");

        BatchResponseDTO dto = new BatchResponseDTO();
        dto.setId(batchId);
        dto.setBatchName("Test Batch");

        when(testBatchRepository.findAllByCriteria("", null)).thenReturn(List.of(batch));
        when(batchMapper.toDto(batch)).thenReturn(dto);
        List<BatchResponseDTO> result = testBatchService.getAllBatchesByCriteria("", null);

        System.out.println(result);
        assertEquals(1, result.size());
        assertEquals("Test Batch", result.get(0).getBatchName());
    }


    @Test
    void TEST_getEmailsForBatch_ValidBatchWithEmailsShouldReturnEmails(){
        UUID batchId = UUID.randomUUID();
        List<String> emails = Arrays.asList("user1@example.com", "user2@example.com", "user3@example.com");


        TestBatch batch = new TestBatch();
        batch.setBatchId(batchId);
        batch.setBatchName("Test Batch");

        List<TestBatchEmailList> emailList = new ArrayList<>();

        for (String email : emails) {
            TestBatchEmailList item = new TestBatchEmailList();
            item.setBatchId(batchId);
            item.setEmail(email);
            emailList.add(item);
        }

        when(testBatchEmailListsRepository.findAllByBatchId(batchId)).thenReturn(emailList);

        List<String> result = testBatchService.getEmailsForBatch(batchId);

        assertNotNull(result);
        assertEquals(emails.size(), result.size());
        assertTrue(result.containsAll(emails));
        verify(testBatchEmailListsRepository, times(1)).findAllByBatchId(batchId);
    }

    @Test
    void TEST_getEmailsForBatch_EmptyBatchShouldReturnEmptyEmails(){
        UUID batchId = UUID.randomUUID();

        when(testBatchEmailListsRepository.findAllByBatchId(batchId)).thenReturn(Collections.emptyList());

        List<String> result = testBatchService.getEmailsForBatch(batchId);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(testBatchEmailListsRepository, times(1)).findAllByBatchId(batchId);
    }


    @Test
    void TEST_getEmailsForBatch_NonExistentBatchShouldReturnEmptyList() {
        UUID batchId = UUID.randomUUID();

        when(testBatchEmailListsRepository.findAllByBatchId(batchId)).thenReturn(Collections.emptyList());

        List<String> result = testBatchService.getEmailsForBatch(batchId);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void TEST_updateEmailsForBatch_ValidEmailsShouldDeleteOldAndSaveNew() {
        UUID batchId = UUID.randomUUID();
        List<String> newEmails = Arrays.asList("new1@example.com", "new2@example.com");

        testBatchService.updateEmailsForBatch(batchId, newEmails);

        verify(testBatchEmailListsRepository, times(1)).deleteAllByBatchId(batchId);
        verify(testBatchEmailListsRepository, times(1)).saveAll(argThat(iterable -> {
            List<TestBatchEmailList> list = (List<TestBatchEmailList>) iterable;
            return list.size() == newEmails.size() &&
                    list.stream().allMatch(item -> item.getBatchId().equals(batchId));
        }));
    }

    @Test
    void TEST_updateEmailsForBatch_SingleEmailShouldWork() {
        UUID batchId = UUID.randomUUID();
        List<String> singleEmail = Collections.singletonList("single@example.com");

        testBatchService.updateEmailsForBatch(batchId, singleEmail);

        verify(testBatchEmailListsRepository, times(1)).deleteAllByBatchId(batchId);
        verify(testBatchEmailListsRepository, times(1)).saveAll(argThat(iterable -> {
            List<TestBatchEmailList> list = (List<TestBatchEmailList>) iterable;
            return list.size() == singleEmail.size() &&
                    list.stream().allMatch(item -> item.getBatchId().equals(batchId));
        }));
    }

    @Test
    void TEST_updateEmailsForBatch_DuplicateEmailsShouldSaveAll() {
        UUID batchId = UUID.randomUUID();
        List<String> duplicateEmails = Arrays.asList("dup@example.com", "DUP@example.com", "dup@example.com");

        testBatchService.updateEmailsForBatch(batchId, duplicateEmails);

        verify(testBatchEmailListsRepository, times(1)).saveAll(argThat(iterable -> {
            List<TestBatchEmailList> list = (List<TestBatchEmailList>) iterable;
            return list.size() == 1 &&
                    list.stream().allMatch(item -> item.getBatchId().equals(batchId));
        }));
    }
}

