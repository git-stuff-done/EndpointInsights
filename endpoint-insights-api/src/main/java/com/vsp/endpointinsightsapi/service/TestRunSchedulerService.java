package com.vsp.endpointinsightsapi.service;

import com.vsp.endpointinsightsapi.config.TestRunsConfig;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;

@Service
public class TestRunSchedulerService {

    private static final Logger LOG = LoggerFactory.getLogger(TestRunSchedulerService.class);

    private final TaskScheduler taskScheduler;
    private final TestRunsConfig testRunsConfig;
    private final TestRunService testRunService;

    private ScheduledFuture<?> purgeFuture;

    public TestRunSchedulerService(TaskScheduler taskScheduler,
                                   TestRunsConfig testRunsConfig,
                                   TestRunService testRunService) {
        this.taskScheduler = taskScheduler;
        this.testRunsConfig = testRunsConfig;
        this.testRunService = testRunService;
    }


    @PostConstruct
    public void init() {
        LOG.info("TestRunSchedulerService initialized");
        Duration duration = Duration.ofHours(testRunsConfig.getPurgeFrequencyHours()).plusMinutes(testRunsConfig.getPurgeFrequencyMinutes());

        purgeFuture = taskScheduler.scheduleAtFixedRate(this::runPurge, duration);
    }

    private void runPurge() {
        Instant oldestTestRun = Instant.now()
                .minus(testRunsConfig.getMaxAgeMonths(), ChronoUnit.MONTHS)
                .minus(testRunsConfig.getMaxAgeDays(), ChronoUnit.DAYS)
                .minus(testRunsConfig.getMaxAgeHours(), ChronoUnit.HOURS)
                .minus(testRunsConfig.getMaxAgeMinutes(), ChronoUnit.MINUTES)
                .minus(testRunsConfig.getMaxAgeSeconds(), ChronoUnit.SECONDS);

        try {
            ResponseEntity<Map<String, Object>> status = testRunService.deleteBefore(oldestTestRun);
            LOG.info("Purged {} test runs", status.getBody().get("deletedRuns"));
        } catch (Exception e) {
            LOG.error("Error purging old test runs", e);
        }

    }


}
