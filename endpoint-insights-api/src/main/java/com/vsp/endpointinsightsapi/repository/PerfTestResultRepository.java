package com.vsp.endpointinsightsapi.repository;

import com.vsp.endpointinsightsapi.model.entity.PerfTestResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PerfTestResultRepository extends JpaRepository<PerfTestResult, Integer> {

	Optional<PerfTestResult> findById(Integer runId);

}
