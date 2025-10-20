package com.vsp.endpointinsightsapi.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "test_batch")
public class TestBatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //TODO: Create ManyToMany with jobs entity once made

    @Column(name = "batch_name", nullable = false)
    String batchName;

    @Column(name = "schedule_id")
    Long scheduleId;

    @Column(name = "start_time")
    @Temporal(TemporalType.DATE)
    LocalDateTime startTime;

    @Temporal(TemporalType.DATE)
    @Column(name = "last_time_run")
    LocalDateTime lastTimeRun;

    @Column(name = "active")
    Boolean active;
}
