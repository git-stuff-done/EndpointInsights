package com.vsp.endpointinsightsapi.repository;

import com.vsp.endpointinsightsapi.model.TestBatch;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TestBatchRepository extends JpaRepository<TestBatch, Long> {

    public TestBatch findByTestId(UUID testId);
}
