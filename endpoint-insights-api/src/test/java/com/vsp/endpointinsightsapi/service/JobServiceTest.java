package com.vsp.endpointinsightsapi.service;

import com.vsp.endpointinsightsapi.model.Job;
import com.vsp.endpointinsightsapi.exception.JobNotFoundException;
import com.vsp.endpointinsightsapi.repository.JobRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class JobServiceTest {

    @Mock
    private JobRepository jobRepository;

    @InjectMocks
    private JobService jobService;
        //test if job exists it is deleted

    @Test
    void createJob_returnSavedJob() {
        Job job = new Job();
        when(jobRepository.save(job)).thenReturn(job);
        Job testResult = jobService.createJob(job);
        assertNotNull(testResult);
        verify(jobRepository, times(1)).save(job);
    }
    
    @Test
    void getAllJobs_ReturnListOfJobs() {
        Job job1 = new Job();
        job1.setJobId("1");
        Job job2 = new Job();
        job2.setJobId("2");

        when(jobRepository.findAll()).thenReturn(Arrays.asList(job1, job2));
        List<Job> testResult = jobService.getAllJobs();
        assertEquals(2, testResult.size());
        verify(jobRepository, times(1)).findAll();
    }
    //test if job does not exist throws exception

    @Test
    void getJobById_ReturnJob() {
        String jobId = "1";
        Job job = new Job();
        job.setJobId(jobId);

        when(jobRepository.existsById(eq(jobId))).thenReturn(true);
        when(jobRepository.findById(eq(jobId))).thenReturn(Optional.of(job));
        Job testResult = jobService.getJobById(jobId);
        assertNotNull(testResult);
        assertEquals(jobId, testResult.getJobId());
        verify(jobRepository, times(1)).existsById(jobId);
        verify(jobRepository, times(1)).findById(jobId);
    }

    @Test
    void getJobById_JobNotFound_ThrowsException() {
        String jobId = "nonexistent";
        when(jobRepository.existsById(eq(jobId))).thenReturn(false);
        Exception exception = assertThrows(JobNotFoundException.class, () -> {
            jobService.getJobById(jobId);
        });
        assertEquals("Job not found: " + jobId, exception.getMessage());
        verify(jobRepository, times(1)).existsById(jobId);
        verify(jobRepository, times(0)).findById(anyString());
    }
}