package com.vsp.endpointinsightsapi.service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.vsp.endpointinsightsapi.exception.JobNotFoundException;
import com.vsp.endpointinsightsapi.model.Job;
import com.vsp.endpointinsightsapi.model.JobCreateRequest;
import com.vsp.endpointinsightsapi.model.enums.TestType;
import com.vsp.endpointinsightsapi.repository.JobRepository;

@ExtendWith(MockitoExtension.class)
class JobServiceTest {

    @Mock
    private JobRepository jobRepository;

    @Mock
    private GitRepositoryService gitRepositoryService;

    @InjectMocks
    private JobService jobService;
        //test if job exists it is deleted

    @Test
    void createJob_returnSavedJobStatus() throws Exception {
        JobCreateRequest jobRequest = new JobCreateRequest();
        jobRequest.setName("Test Job");
        jobRequest.setDescription("Test Description");
        jobRequest.setTestType(TestType.INTEGRATION);
        
        Job savedJob = new Job();
        savedJob.setJobId(UUID.randomUUID());
        savedJob.setName(jobRequest.getName());
        
        when(jobRepository.save(any(Job.class))).thenReturn(savedJob);
        Job testResult = jobService.createJob(jobRequest);
        assertNotNull(testResult);
        assertNotNull(testResult.getJobId());
        assertEquals("Test Job", testResult.getName());
        verify(jobRepository, times(1)).save(any(Job.class));
    }
    
    @Test
    void getAllJobs_ReturnListOfJobs() {
        Job job1 = new Job();
        UUID job1Id = UUID.randomUUID();
        job1.setJobId(job1Id);
        jobRepository.save(job1);
        Job job2 = new Job();
        UUID job2Id = UUID.randomUUID();
        job2.setJobId(job2Id);
        jobRepository.save(job2);

        when(jobRepository.findAll()).thenReturn(Arrays.asList(job1, job2));
        Optional<List<Job>> testResult = jobService.getAllJobs();
        assertEquals(2, testResult.get().size());
        verify(jobRepository, times(2)).findAll();
    }
    //test if job does not exist throws exception

    @Test
    void getJobById_ReturnJob() {
        UUID jobId = UUID.randomUUID();
        Job job = new Job();
        job.setJobId(jobId);
        when(jobRepository.existsById(jobId)).thenReturn(true);
        when(jobRepository.findById(jobId)).thenReturn(Optional.of(job));
        Optional<Job> testResult = jobService.getJobById(jobId);
        assertNotNull(testResult);
        assertTrue(testResult.isPresent());
        assertEquals(jobId, testResult.get().getJobId());
        verify(jobRepository, times(1)).findById(jobId);
        verify(jobRepository, times(1)).existsById(jobId);
    }

    @Test
    void getJobById_JobNotFound_ThrowsException() {
        UUID jobId = UUID.randomUUID();
        when(jobRepository.existsById(jobId)).thenReturn(false);
        JobNotFoundException exception = assertThrows(JobNotFoundException.class, () -> {
            jobService.getJobById(jobId);
        });
        assertEquals("Job not found with ID: " + jobId.toString(), exception.getErrorResponse().getDescription());
    }

    @Test
    void deleteJobById_existingJob_deletesSuccessfully() {

        UUID jobId = UUID.randomUUID();
        when(jobRepository.existsById(jobId)).thenReturn(true);

        jobService.deleteJobById(jobId);

        verify(jobRepository, times(1)).deleteById(jobId);
        verify(jobRepository, times(1)).existsById(jobId);
    }

    //test if job does not exist throws exception

    @Test
    void deleteJobById_nonExistingJob_throwsJobNotFoundException() {
        UUID jobId = UUID.randomUUID();
        when(jobRepository.existsById(jobId)).thenReturn(false);

        JobNotFoundException exception = assertThrows(
                JobNotFoundException.class, () -> jobService.deleteJobById(jobId)
        );

        assertEquals("Job not found with ID: " + jobId.toString(), exception.getErrorResponse().getDescription());
        }

    @Test
    void updateExistingJob(){
        // Create job with ID
        Job job = new Job();
        job.setJobId(UUID.randomUUID());

        // Stub the save to return the job with ID
        when(jobRepository.save(any(Job.class))).thenReturn(job);

        // Now this returns the job
        Job saved = jobRepository.save(job);
        verify(jobRepository, times(1)).save(any(Job.class));

        // Stub findById
        when(jobRepository.findById(saved.getJobId())).thenReturn(Optional.of(saved));

        Job existing = jobRepository.findById(saved.getJobId())
                .orElseThrow(() -> new JobNotFoundException(saved.getJobId().toString()));

        existing.setName("changedName");

        // Stub save again to return updated job
        when(jobRepository.save(existing)).thenReturn(existing);

        jobRepository.save(existing);
        verify(jobRepository, times(2)).save(any(Job.class));

        // Stub findById to return updated job
        when(jobRepository.findById(saved.getJobId())).thenReturn(Optional.of(existing));

        existing = jobRepository.findById(saved.getJobId())
                .orElseThrow(() -> new JobNotFoundException(saved.getJobId().toString()));

        assertEquals("changedName", existing.getName());
    }

    @Test
    void updateJob_withGitAuthFields_updatesSuccessfully() {
        UUID jobId = UUID.randomUUID();
        Job existingJob = new Job();
        existingJob.setJobId(jobId);
        existingJob.setName("original");

        Job updateData = new Job();
        updateData.setJobId(jobId);
        updateData.setName("updated");
        updateData.setDescription("updated description");
        updateData.setGitUrl("https://github.com/test/repo.git");
        updateData.setGitAuthType(com.vsp.endpointinsightsapi.model.enums.GitAuthType.BASIC);
        updateData.setGitUsername("testuser");
        updateData.setGitPassword("testpass");
        updateData.setGitSshPrivateKey("ssh-key");
        updateData.setGitSshPassphrase("passphrase");

        when(jobRepository.findById(jobId)).thenReturn(Optional.of(existingJob));
        when(jobRepository.save(any(Job.class))).thenReturn(existingJob);

        Job result = jobService.updateJob(jobId, updateData);

        assertNotNull(result);
        assertEquals("updated", existingJob.getName());
        assertEquals("updated description", existingJob.getDescription());
        assertEquals("https://github.com/test/repo.git", existingJob.getGitUrl());
        assertEquals(com.vsp.endpointinsightsapi.model.enums.GitAuthType.BASIC, existingJob.getGitAuthType());
        assertEquals("testuser", existingJob.getGitUsername());
        assertEquals("testpass", existingJob.getGitPassword());
        assertEquals("ssh-key", existingJob.getGitSshPrivateKey());
        assertEquals("passphrase", existingJob.getGitSshPassphrase());
        verify(jobRepository, times(1)).findById(jobId);
        verify(jobRepository, times(1)).save(existingJob);
    }

    @Test
    void updateJob_jobNotFound_throwsException() {
        UUID jobId = UUID.randomUUID();
        Job updateData = new Job();
        updateData.setJobId(jobId);

        when(jobRepository.findById(jobId)).thenReturn(Optional.empty());

        JobNotFoundException exception = assertThrows(JobNotFoundException.class, () -> {
            jobService.updateJob(jobId, updateData);
        });

        assertEquals("Job not found with ID: " + jobId.toString(), exception.getErrorResponse().getDescription());
        verify(jobRepository, times(1)).findById(jobId);
        verify(jobRepository, times(0)).save(any());
    }

    @Test
    void checkoutJobRepository_success() {
        UUID jobId = UUID.randomUUID();
        Job job = new Job();
        job.setJobId(jobId);
        job.setGitUrl("https://github.com/test/repo.git");

        java.nio.file.Path mockPath = java.nio.file.Paths.get("/tmp/checkout");

        when(jobRepository.findById(jobId)).thenReturn(Optional.of(job));
        when(gitRepositoryService.checkoutJobRepository(job)).thenReturn(mockPath);

        java.nio.file.Path result = jobService.checkoutJobRepository(jobId);

        assertNotNull(result);
        assertEquals(mockPath, result);
        verify(jobRepository, times(1)).findById(jobId);
        verify(gitRepositoryService, times(1)).checkoutJobRepository(job);
    }

    @Test
    void checkoutJobRepository_jobNotFound_throwsException() {
        UUID jobId = UUID.randomUUID();
        when(jobRepository.findById(jobId)).thenReturn(Optional.empty());

        JobNotFoundException exception = assertThrows(JobNotFoundException.class, () -> {
            jobService.checkoutJobRepository(jobId);
        });

        assertEquals("Job not found with ID: " + jobId.toString(), exception.getErrorResponse().getDescription());
        verify(jobRepository, times(1)).findById(jobId);
        verify(gitRepositoryService, times(0)).checkoutJobRepository(any());
    }


}
