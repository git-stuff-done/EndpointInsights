package com.vsp.endpointinsightsapi.service;

import com.vsp.endpointinsightsapi.model.TestBatch;
import com.vsp.endpointinsightsapi.model.entity.TestResult;
import com.vsp.endpointinsightsapi.model.entity.TestRun;
import com.vsp.endpointinsightsapi.repository.TestBatchRepository;
import com.vsp.endpointinsightsapi.repository.TestResultRepository;
import com.vsp.endpointinsightsapi.repository.TestRunRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class BatchRunPersistenceService {

	private final TestBatchRepository testBatchRepository;
	private final TestRunRepository testRunRepository;
    private final TestResultRepository testResultRepository;

	public BatchRunPersistenceService(TestBatchRepository testBatchRepository, TestRunRepository testRunRepository, TestResultRepository testResultRepository) {
		this.testBatchRepository = testBatchRepository;
		this.testRunRepository = testRunRepository;
        this.testResultRepository = testResultRepository;
    }

	@Transactional
	public void save(TestBatch batch, TestRun run) {
		testRunRepository.save(run);
		testBatchRepository.save(batch);

	}

    @Transactional
    public List<TestResult> loadResultsWithPerf(List<TestResult> testResults) {
        return testResults.stream()
                .map(r -> {
                    if (r.getId() == null) return r;
                    return testResultRepository.findById(r.getId()).orElse(r);
                })
                .toList();
    }

}
