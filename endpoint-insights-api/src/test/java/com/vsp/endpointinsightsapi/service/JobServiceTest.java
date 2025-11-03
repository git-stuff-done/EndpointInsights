package com.vsp.endpointinsightsapi.service;

import com.vsp.endpointinsightsapi.model.Job;
import com.vsp.endpointinsightsapi.exception.JobNotFoundException;
import com.vsp.endpointinsightsapi.repository.JobRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

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
        Job job = new Job();
        when(jobRepository.save(job)).thenReturn(job);
        Job testResult = jobService.createJob(job);
        assertNotNull(testResult);
        verify(jobRepository, times(1)).save(job);
    }
    
    @Test
    void getAllJobs_ReturnListOfJobs() {
        Job job1 = new Job();
        job1.setJobId(UUID.fromString("1"));
        jobRepository.save(job1);
        Job job2 = new Job();
        job2.setJobId(UUID.fromString("2"));
        jobRepository.save(job2);

        when(jobRepository.findAll()).thenReturn(Arrays.asList(job1, job2));
        Optional<List<Job>> testResult = jobService.getAllJobs();
        assertEquals(2, testResult.get().size());
        verify(jobRepository, times(1)).findAll();
    }
    //test if job does not exist throws exception

    @Test
    void getJobById_ReturnJob() {
        UUID jobId = UUID.fromString("6");
        Job job = new Job();
        job.setJobId(jobId);

        // when(jobRepository.existsById(eq(jobId))).thenReturn(true);
        // when(jobRepository.findById(eq(jobId))).thenReturn(Optional.of(job));
        // Job testResult = jobService.getJobById(jobId);
        // assertNotNull(testResult);
        // assertEquals(jobId, testResult.getJobId());
        // verify(jobRepository, times(1)).existsById(jobId);
        // verify(jobRepository, times(1)).findById(jobId);
        when(jobRepository.existsById(jobId)).thenReturn(true);
        Optional<Job> testResult = jobService.getJobById(jobId);
        assertNotNull(testResult);
        assertEquals(jobId, testResult.get().getJobId());
        verify(jobRepository, times(1)).findById(jobId);
        verify(jobRepository, times(1)).existsById(jobId);
    }

    @Test
    void getJobById_JobNotFound_ThrowsException() {
        when(jobRepository.existsById(UUID.fromString("2"))).thenReturn(false);
        JobNotFoundException exception = assertThrows(JobNotFoundException.class, () -> {
            jobService.getJobById(UUID.fromString("2"));
        });
        assertEquals("Job not found with ID: 2", exception.getMessage());
    }

    @Test
    void deleteJobById_existingJob_deletesSuccessfully() {

        when(jobRepository.existsById(UUID.fromString("1"))).thenReturn(true);

        jobService.deleteJobById(UUID.fromString("1"));

        verify(jobRepository, times(1)).deleteById(UUID.fromString("1"));
        verify(jobRepository, times(1)).existsById(UUID.fromString("1"));
    }

    //test if job does not exist throws exception

    @Test
    void deleteJobById_nonExistingJob_throwsJobNotFoundException() {
        when(jobRepository.existsById(UUID.fromString("2"))).thenReturn(false);

        JobNotFoundException exception = assertThrows(
                JobNotFoundException.class, () -> jobService.deleteJobById(UUID.fromString("2"))
        );

        assertEquals("Job not found with ID: 2", exception.getErrorResponse().getDescription());
        }

}
