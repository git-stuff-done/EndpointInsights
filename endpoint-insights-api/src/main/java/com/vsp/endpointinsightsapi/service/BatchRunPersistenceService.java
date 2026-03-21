package com.vsp.endpointinsightsapi.service;

import com.vsp.endpointinsightsapi.model.TestBatch;
import com.vsp.endpointinsightsapi.model.entity.TestRun;
import com.vsp.endpointinsightsapi.repository.TestBatchRepository;
import com.vsp.endpointinsightsapi.repository.TestRunRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class BatchRunPersistenceService {

	private final TestBatchRepository testBatchRepository;
	private final TestRunRepository testRunRepository;

	public BatchRunPersistenceService(TestBatchRepository testBatchRepository, TestRunRepository testRunRepository) {
		this.testBatchRepository = testBatchRepository;
		this.testRunRepository = testRunRepository;
	}

	@Transactional
	public void save(TestBatch batch, TestRun run) {
		testBatchRepository.save(batch);
		testRunRepository.save(run);
	}

}
