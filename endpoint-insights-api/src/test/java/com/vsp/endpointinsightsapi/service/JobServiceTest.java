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
        Job job = new Job();
        job.setJobId("1");
        when(jobRepository.findById("1")).thenReturn(Optional.of(job));
        Optional<Job> testResult = jobService.getJobById("1");
        assertTrue(testResult.isPresent());
        assertEquals("1", testResult.get().getJobId());
        verify(jobRepository, times(1)).findById("1");
    }
}