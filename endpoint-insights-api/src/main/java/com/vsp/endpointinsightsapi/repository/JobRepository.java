package com.vsp.endpointinsightsapi.repository;
import com.vsp.endpointinsightsapi.model.Job;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JobRepository extends JpaRepository<Job, UUID> {
}