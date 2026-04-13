package com.vsp.endpointinsightsapi.repository;

import com.vsp.endpointinsightsapi.model.entity.TestResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TestResultRepository extends JpaRepository<TestResult, UUID> {

	Optional<TestResult> findById(UUID runId);

	/**
	 * This gets all jobs by job/test type.
	 * @see com.vsp.endpointinsightsapi.model.enums.TestType
	* */
	List<TestResult> getAllByJobType(Integer jobType);

	@Modifying
	@Query("DELETE FROM TestResult tr WHERE tr.testRun.runId IN :runIds")
	void deleteByRunIds(@Param("runIds") List<UUID> runIds);
}