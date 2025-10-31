package com.vsp.endpointinsightsapi.service;

import com.vsp.endpointinsightsapi.exception.JobNotFoundException;
import com.vsp.endpointinsightsapi.repository.JobRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class JobServiceTest {

    @Mock
    private JobRepository jobRepository;

    @InjectMocks
    private JobService jobService;
        //test if job exists it is deleted
    @Test
    void deleteJobById_existingJob_deletesSuccessfully() {

        when(jobRepository.existsById("1")).thenReturn(true);

        jobService.deleteJobById("1");

        verify(jobRepository, times(1)).deleteById("1");
        verify(jobRepository, times(1)).existsById("1");
    }

    //test if job does not exist throws exception

    @Test
    void deleteJobById_nonExistingJob_throwsJobNotFoundException() {
        when(jobRepository.existsById("2")).thenReturn(false);

        JobNotFoundException exception = assertThrows(
                JobNotFoundException.class, () -> jobService.deleteJobById("2")
        );

        assertEquals("Job not found with ID: 2", exception.getErrorResponse().getDescription());
        }

}
