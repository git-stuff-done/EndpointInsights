package com.vsp.endpointinsightsapi.service;

import com.vsp.endpointinsightsapi.model.entity.TestRun;
import com.vsp.endpointinsightsapi.exception.JobNotFoundException;
import com.vsp.endpointinsightsapi.exception.TestRunNotFoundException;
import com.vsp.endpointinsightsapi.repository.JobRepository;
import com.vsp.endpointinsightsapi.repository.TestRunRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class TestRunService {

	private static final Logger LOG = LoggerFactory.getLogger(TestRunService.class);

	private final TestRunRepository testRunRepository;
	private final JobRepository jobRepository;

	public TestRunService(TestRunRepository testRunRepository, JobRepository jobRepository) {
		this.testRunRepository = testRunRepository;
		this.jobRepository = jobRepository;
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

	public TestRun getTestRunById(UUID runId) {
		return testRunRepository.findById(runId)
				.orElseThrow(() -> new TestRunNotFoundException(runId.toString()));
	}

	public void deleteTestRunById(UUID runId) {
		if (!testRunRepository.existsById(runId)) {
			throw new TestRunNotFoundException(runId.toString());
		}
		testRunRepository.deleteById(runId);
	}
}
