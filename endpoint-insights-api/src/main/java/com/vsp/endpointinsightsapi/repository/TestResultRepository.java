package com.vsp.endpointinsightsapi.repository;

import com.vsp.endpointinsightsapi.model.entity.TestResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TestResultRepository extends JpaRepository<TestResult, Integer> {

	Optional<TestResult> findById(Integer runId);

	/**
	 * This gets all jobs by job/test type.
	 * @see com.vsp.endpointinsightsapi.model.enums.TestType
	* */
	List<TestResult> getAllByJobType(Integer jobType);
}
