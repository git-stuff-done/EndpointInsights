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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
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
        job.setJobId(UUID.randomUUID());
        assertNotNull(job.getJobId(), "Job ID should be generated");
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


}
