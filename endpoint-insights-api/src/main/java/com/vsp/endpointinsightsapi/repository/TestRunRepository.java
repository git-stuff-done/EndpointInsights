package com.vsp.endpointinsightsapi.repository;

import com.vsp.endpointinsightsapi.model.entity.TestResult;
import com.vsp.endpointinsightsapi.model.entity.TestRun;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TestRunRepository extends JpaRepository<TestRun, UUID> {
	Page<TestRun> findAllByOrderByFinishedAtDesc(Pageable pageable);

    Page<TestRun> findByJobIdOrderByFinishedAtDesc(UUID jobId, Pageable pageable);

    Page<TestRun> findByBatchIdOrderByFinishedAtDesc(UUID batchId, Pageable pageable);
	List<TestRun> findTop10ByBatchIdOrderByStartedAtDesc(UUID batchId);

}
