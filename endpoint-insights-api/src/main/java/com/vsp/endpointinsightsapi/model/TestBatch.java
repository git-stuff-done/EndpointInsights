package com.vsp.endpointinsightsapi.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "test_batch")
public class TestBatch {

    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

//   @ManyToMany
//   @JoinTable(name = )
//   private List<Job> jobs;

    @Column(name = "batch_name", nullable = false)
    String batchName;

    @Column(name = "schedule_id")
    Long scheduleId;

    @Column(name = "start_time")
    LocalDate startTime;

    @Column(name = "last_time_run")
    LocalDate lastTimeRun;

    @Column(name = "active")
    Boolean active;
}
