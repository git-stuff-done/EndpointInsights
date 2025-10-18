package com.example.endpointinsights.repository;

import com.example.endpointinsights.model.JobSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JobScheduleRepository extends JpaRepository<JobSchedule, Long> {

}