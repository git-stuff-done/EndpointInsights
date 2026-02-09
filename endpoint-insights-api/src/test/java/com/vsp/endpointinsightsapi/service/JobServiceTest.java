package com.vsp.endpointinsightsapi.service;

import com.vsp.endpointinsightsapi.model.Job;
import com.vsp.endpointinsightsapi.model.JobCreateRequest;
import com.vsp.endpointinsightsapi.model.enums.TestType;
import com.vsp.endpointinsightsapi.exception.JobNotFoundException;
import com.vsp.endpointinsightsapi.repository.JobRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
class JobServiceTest {

    @Mock
    private JobRepository jobRepository;

    @InjectMocks
    private JobService jobService;
        //test if job exists it is deleted

    @Test
    void createJob_returnSavedJobStatus() throws Exception {
        JobCreateRequest createJobDto = new JobCreateRequest("test_job", "test description", "https://github.com/test/test.git", "npm run test", "npm run build", TestType.PERF, null);
        Job job = new Job();
        job.setId(UUID.randomUUID());
        assertNotNull(job.getId(), "Job ID should be generated");
        job.setName(createJobDto.getName());
        assertEquals("test_job", job.getName());
        job.setDescription(createJobDto.getDescription());
        assertEquals("test description", job.getDescription());
        job.setGitUrl(createJobDto.getGitUrl());
        assertEquals("https://github.com/test/test.git", job.getGitUrl());
        job.setRunCommand(createJobDto.getRunCommand());
        assertEquals("npm run test", job.getRunCommand());
        job.setCompileCommand(createJobDto.getCompileCommand());
        assertEquals("npm run build", job.getCompileCommand());
        job.setJobType(createJobDto.getTestType());
        assertEquals(TestType.PERF, job.getJobType());
        job.setConfig(createJobDto.getConfig());
        assertEquals(null, job.getConfig());
        when(jobRepository.save(any())).thenReturn(job);
        Job testResult = jobService.createJob(createJobDto);
        assertNotNull(testResult);
        verify(jobRepository, times(1)).save(any());
    }
    
    @Test
    void getAllJobs_ReturnListOfJobs() {
        Job job1 = new Job();
        UUID job1Id = UUID.randomUUID();
        job1.setId(job1Id);
        jobRepository.save(job1);
        Job job2 = new Job();
        UUID job2Id = UUID.randomUUID();
        job2.setId(job2Id);
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
        job.setId(jobId);
        when(jobRepository.existsById(jobId)).thenReturn(true);
        when(jobRepository.findById(jobId)).thenReturn(Optional.of(job));
        Optional<Job> testResult = jobService.getJobById(jobId);
        assertNotNull(testResult);
        assertTrue(testResult.isPresent());
        assertEquals(jobId, testResult.get().getId());
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
        Job job = new Job();
        job.setId(UUID.randomUUID());

        when(jobRepository.save(any(Job.class))).thenReturn(job);

        Job saved = jobRepository.save(job);
        verify(jobRepository, times(1)).save(any(Job.class));

        when(jobRepository.findById(saved.getId())).thenReturn(Optional.of(saved));

        Job existing = jobRepository.findById(saved.getId())
                .orElseThrow(() -> new JobNotFoundException(saved.getId().toString()));

        existing.setName("changedName");

        when(jobRepository.save(existing)).thenReturn(existing);

        jobRepository.save(existing);
        verify(jobRepository, times(2)).save(any(Job.class));

        when(jobRepository.findById(saved.getId())).thenReturn(Optional.of(existing));

        existing = jobRepository.findById(saved.getId())
                .orElseThrow(() -> new JobNotFoundException(saved.getId().toString()));

        assertEquals("changedName", existing.getName());
    }


}
