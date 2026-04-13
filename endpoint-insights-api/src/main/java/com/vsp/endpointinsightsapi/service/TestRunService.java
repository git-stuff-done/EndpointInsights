package com.vsp.endpointinsightsapi.service;

import com.vsp.endpointinsightsapi.dto.RecentActivityDTO;
import com.vsp.endpointinsightsapi.exception.JobNotFoundException;
import com.vsp.endpointinsightsapi.exception.TestRunNotFoundException;
import com.vsp.endpointinsightsapi.model.Job;
import com.vsp.endpointinsightsapi.model.TestBatch;
import com.vsp.endpointinsightsapi.model.entity.TestRun;
import com.vsp.endpointinsightsapi.model.enums.TestRunStatus;
import com.vsp.endpointinsightsapi.repository.*;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TestRunService {

	private static final Logger LOG = LoggerFactory.getLogger(TestRunService.class);

	private final TestRunRepository testRunRepository;
	private final JobRepository jobRepository;
	private final TestBatchRepository testBatchRepository;
	private final PerfTestResultRepository perfTestResultRepository;
	private final TestResultRepository testResultRepository;

	public TestRunService(TestRunRepository testRunRepository, JobRepository jobRepository, TestBatchRepository testBatchRepository, PerfTestResultRepository perfTestResultRepository, TestResultRepository testResultRepository) {
		this.testRunRepository = testRunRepository;
		this.jobRepository = jobRepository;
		this.testBatchRepository = testBatchRepository;
		this.perfTestResultRepository = perfTestResultRepository;
		this.testResultRepository = testResultRepository;
	}

	public TestRun createTestRun(TestRun testRun) {
		LOG.debug("Creating test run for job {}", testRun.getJobId());

		if (testRun.getJobId() == null || !jobRepository.existsById(testRun.getJobId())) {
			throw new JobNotFoundException(String.valueOf(testRun.getJobId()));
		}
		return testRunRepository.save(testRun);
	}

	public List<TestRun> getRecentTestRuns(int limit) {
		int safeLimit = Math.max(1, Math.min(limit, 100));
		return testRunRepository
				.findAllByOrderByFinishedAtDesc(PageRequest.of(0, safeLimit, Sort.by(Sort.Direction.DESC, "finishedAt")))
				.getContent();
	}

	public List<RecentActivityDTO> getRecentActivity(int limit) {
		int safeLimit = Math.max(1, Math.min(limit, 100));
		List<TestRun> runs = testRunRepository
				.findAllByOrderByFinishedAtDesc(PageRequest.of(0, safeLimit, Sort.by(Sort.Direction.DESC, "finishedAt")))
				.getContent();

		Set<UUID> jobIds = runs.stream().map(TestRun::getJobId).collect(Collectors.toSet());
		Set<UUID> batchIds = runs.stream()
				.map(TestRun::getBatchId)
				.filter(Objects::nonNull)
				.collect(Collectors.toSet());

		Map<UUID, Job> jobMap = jobRepository.findAllById(jobIds).stream()
				.collect(Collectors.toMap(Job::getJobId, j -> j));
		Map<UUID, TestBatch> batchMap = testBatchRepository.findAllById(batchIds).stream()
				.collect(Collectors.toMap(TestBatch::getBatchId, b -> b));

		return runs.stream().map(run -> {
			Job job = jobMap.get(run.getJobId());
			TestBatch batch = run.getBatchId() != null ? batchMap.get(run.getBatchId()) : null;

			long durationMs = 0;
			if (run.getStartedAt() != null && run.getFinishedAt() != null) {
				durationMs = Duration.between(run.getStartedAt(), run.getFinishedAt()).toMillis();
			}

			return RecentActivityDTO.builder()
					.runId(run.getRunId().toString())
					.jobId(run.getJobId() != null ? run.getJobId().toString() : null)
                    .batchId(run.getBatchId() != null ? run.getBatchId().toString() : null)
					.testName(job != null ? job.getName() : "Unknown")
					.group(deriveScheduleType(batch))
					.dateRun(run.getStartedAt())
					.durationMs(durationMs)
					.startedBy(run.getRunBy())
					.status(toDisplayStatus(run.getStatus()))
					.batchName(batch != null ? batch.getBatchName() : null)
					.build();
		}).collect(Collectors.toList());
	}

	private String toDisplayStatus(TestRunStatus status) {
		return switch (status) {
			case COMPLETED -> "PASS";
			case FAILED -> "FAIL";
			default -> status.name();
		};
	}

	private String deriveScheduleType(TestBatch batch) {
		if (batch == null || batch.getCronExpression() == null) return "N/A";
		String[] parts = batch.getCronExpression().trim().split("\\s+");
		if (parts.length < 6) return "Custom";
		String dayOfWeek = parts[5];
		return ("*".equals(dayOfWeek) || "?".equals(dayOfWeek)) ? "Daily" : "Weekly";
	}

	public TestRun getTestRunById(UUID runId) {
		return testRunRepository.findById(runId)
				.orElseThrow(() -> new TestRunNotFoundException(runId.toString()));
	}

	@Transactional
	public ResponseEntity<Map<String, Object>> deleteTestRunById(UUID runId) {
		if (!testRunRepository.existsById(runId)) {
			throw new TestRunNotFoundException(runId.toString());
		}

		perfTestResultRepository.deleteByRunIds(List.of(runId));
		testResultRepository.deleteByRunIds(List.of(runId));
		testRunRepository.deleteById(runId);

		return ResponseEntity.ok(Map.of("status", String.format("Test run %s deleted successfully", runId)));
	}

    // jobId takes precedence over batchId
    public List<RecentActivityDTO> getRecentActivityById(UUID jobId, UUID batchId, int limit) {

        int safeLimit = Math.clamp(limit, 1, 100);

        List<TestRun> runs;

        if (jobId != null) {
            runs = testRunRepository
                    .findByJobIdOrderByFinishedAtDesc(
                            jobId,
                            PageRequest.of(0, safeLimit, Sort.by(Sort.Direction.DESC, "finishedAt"))
                    )
                    .getContent();
        } else if (batchId != null) {
            runs = testRunRepository
                    .findByBatchIdOrderByFinishedAtDesc(
                            batchId,
                            PageRequest.of(0, safeLimit, Sort.by(Sort.Direction.DESC, "finishedAt"))
                    )
                    .getContent();
        } else {
            throw new IllegalArgumentException("Must provide jobId or batchId");
        }

        Set<UUID> batchIds = runs.stream()
                .map(TestRun::getBatchId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Set<UUID> jobIds = runs.stream()
                .map(TestRun::getJobId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<UUID, Job> jobMap = jobRepository.findAllById(jobIds).stream()
                .collect(Collectors.toMap(Job::getJobId, j -> j));



        Map<UUID, TestBatch> batchMap = testBatchRepository.findAllById(batchIds).stream()
                .collect(Collectors.toMap(TestBatch::getBatchId, b -> b));

        return runs.stream().map(run -> {
            Job job = run.getJobId() != null ? jobMap.get(run.getJobId()) : null;
            TestBatch batch = run.getBatchId() != null ? batchMap.get(run.getBatchId()) : null;

            long durationMs = 0;
            if (run.getStartedAt() != null && run.getFinishedAt() != null) {
                durationMs = Duration.between(run.getStartedAt(), run.getFinishedAt()).toMillis();
            }

            return RecentActivityDTO.builder()
                    .runId(run.getRunId().toString())
                    .jobId(run.getJobId() != null ? run.getJobId().toString() : null)
                    .batchId(run.getBatchId() != null ? run.getBatchId().toString() : null)
                    .testName(job != null ? job.getName() : "Unknown")
                    .group(deriveScheduleType(batch))
                    .dateRun(run.getStartedAt())
                    .durationMs(durationMs)
                    .startedBy(run.getRunBy())
                    .status(toDisplayStatus(run.getStatus()))
                    .build();
        }).collect(Collectors.toList());
    }

	@Transactional
    public ResponseEntity<Map<String, Object>> deleteBefore(Instant purgeDate) {
        var oldRuns = testRunRepository.findByFinishedAtBefore(purgeDate).stream().map(TestRun::getRunId).toList();

		perfTestResultRepository.deleteByRunIds(oldRuns);
		testResultRepository.deleteByRunIds(oldRuns);
		testRunRepository.deleteAllByIdInBatch(oldRuns);

		return ResponseEntity.ok(Map.of("deletedRuns", oldRuns.size()));
    }
}
