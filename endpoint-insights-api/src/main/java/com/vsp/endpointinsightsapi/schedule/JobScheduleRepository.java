package com.vsp.endpointinsightsapi.schedule;

import com.vsp.endpointinsightsapi.schedule.JobSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JobScheduleRepository extends JpaRepository<JobSchedule, Long> {

}