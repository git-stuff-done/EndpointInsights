package com.vsp.endpointinsightsapi.service;

import com.vsp.endpointinsightsapi.model.entity.TestRun;
import com.vsp.endpointinsightsapi.repository.TestRunRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TestRunService {

	private static final Logger LOG = LoggerFactory.getLogger(TestRunService.class);

	private final TestRunRepository testRunRepository;

	public TestRunService(TestRunRepository testRunRepository) {
		this.testRunRepository = testRunRepository;
	}

	public TestRun createTestRun(TestRun testRun) {
		LOG.debug("Creating test run for job {}", testRun.getJobId());
		return testRunRepository.save(testRun);
	}

	public List<TestRun> getRecentTestRuns(int limit) {
		int safeLimit = Math.max(1, Math.min(limit, 100));
		return testRunRepository
				.findAllByOrderByFinishedAtDesc(PageRequest.of(0, safeLimit, Sort.by(Sort.Direction.DESC, "finishedAt")))
				.getContent();
	}
}
