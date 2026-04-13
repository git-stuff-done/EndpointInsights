package com.vsp.endpointinsightsapi.service;


import com.vsp.endpointinsightsapi.config.TestRunsConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.TaskScheduler;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@TestComponent
public class TestRunSchedulerServiceTest {

    private TaskScheduler taskScheduler = Mockito.mock(TaskScheduler.class);
    private TestRunsConfig testRunsConfig = Mockito.mock(TestRunsConfig.class);
    private TestRunService testRunService = Mockito.mock(TestRunService.class);

    private TestRunSchedulerService service;

    @BeforeEach
    void setUp() {
        when(testRunsConfig.getPurgeFrequencyHours()).thenReturn(1);
        when(testRunsConfig.getPurgeFrequencyMinutes()).thenReturn(30);
        service = new TestRunSchedulerService(taskScheduler, testRunsConfig, testRunService);
    }

    @Test
    void init_schedulesAtCorrectRate() {
        service.init();

        ArgumentCaptor<Duration> durationArgumentCaptor = ArgumentCaptor.forClass(Duration.class);
        verify(taskScheduler).scheduleAtFixedRate(any(Runnable.class), durationArgumentCaptor.capture());
        assertThat(durationArgumentCaptor.getValue()).isEqualTo(Duration.ofHours(1).plusMinutes(30));

    }

    @Test
    void runPurge_callsDeleteBeforeWithCorrectCutoff() {
        when(testRunsConfig.getMaxAgeDays()).thenReturn(7);
        when(testRunsConfig.getMaxAgeHours()).thenReturn(0);
        when(testRunsConfig.getMaxAgeMinutes()).thenReturn(0);
        when(testRunsConfig.getMaxAgeSeconds()).thenReturn(0);
        when(testRunService.deleteBefore(any())).thenReturn(
                ResponseEntity.ok(Map.of("deletedRuns", 3))
        );

        ArgumentCaptor<Runnable> runnableCaptor = ArgumentCaptor.forClass(Runnable.class);
        service.init();
        verify(taskScheduler).scheduleAtFixedRate(runnableCaptor.capture(), any(Duration.class));

        Instant before = Instant.now();
        runnableCaptor.getValue().run();
        Instant after = Instant.now();

        ArgumentCaptor<Instant> instantCaptor = ArgumentCaptor.forClass(Instant.class);
        verify(testRunService).deleteBefore(instantCaptor.capture());

        Instant cutoff = instantCaptor.getValue();
        Instant expectedCutoff = before.minusSeconds(7 * 24 * 3600);
        // Allow a small window for test execution time
        assertThat(cutoff).isBetween(after.minusSeconds(7 * 24 * 3600 + 1), expectedCutoff.plusSeconds(1));
    }

    @Test
    void runPurge_logsErrorWhenDeleteThrows() {
        when(testRunsConfig.getMaxAgeDays()).thenReturn(1);
        when(testRunsConfig.getMaxAgeHours()).thenReturn(0);
        when(testRunsConfig.getMaxAgeMinutes()).thenReturn(0);
        when(testRunsConfig.getMaxAgeSeconds()).thenReturn(0);
        when(testRunService.deleteBefore(any())).thenThrow(new RuntimeException("DB error"));

        ArgumentCaptor<Runnable> runnableCaptor = ArgumentCaptor.forClass(Runnable.class);
        service.init();
        verify(taskScheduler).scheduleAtFixedRate(runnableCaptor.capture(), any(Duration.class));

        runnableCaptor.getValue().run();
    }

}
