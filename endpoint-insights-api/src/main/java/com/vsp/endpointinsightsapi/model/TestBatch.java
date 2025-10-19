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
public class TestBatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //TODO: Create OneToMany with jobs entity once made

    String batchName;
    Long scheduleId;
    LocalDateTime startTime;
    LocalDateTime lastTimeRun;

}
