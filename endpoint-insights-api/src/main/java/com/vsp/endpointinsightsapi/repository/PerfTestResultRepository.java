package com.vsp.endpointinsightsapi.repository;

import com.vsp.endpointinsightsapi.model.entity.PerfTestResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PerfTestResultRepository extends JpaRepository<PerfTestResult, UUID> {

	Optional<PerfTestResult> findById(UUID runId);

	@Modifying
	@Query("DELETE FROM PerfTestResult p WHERE p.testResult.testRun.runId IN :runIds")
	void deleteByRunIds(@Param("runIds") List<UUID> runIds);

}
