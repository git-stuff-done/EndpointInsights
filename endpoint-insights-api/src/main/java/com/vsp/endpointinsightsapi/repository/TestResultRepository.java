package com.vsp.endpointinsightsapi.repository;

import com.vsp.endpointinsightsapi.model.entity.TestResult;
import org.aspectj.weaver.ast.Test;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TestResultRepository extends JpaRepository<TestResult, UUID> {

	Optional<TestResult> findById(UUID runId);


    @Query("SELECT t FROM TestResult t WHERE t.testRun.runId = cast(:runId as uuid)")
    Optional<List<TestResult>> findByRunId(@Param("runId") UUID runId);

	/**
	 * This gets all jobs by job/test type.
	 * @see com.vsp.endpointinsightsapi.model.enums.TestType
	* */
	List<TestResult> getAllByJobType(Integer jobType);
}