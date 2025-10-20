package com.vsp.endpointinsightsapi.testbatchtable;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Date;

import lombok.Getter;
import lombok.Setter;

//import com.vsp.endpointinsightsapi.schedule.JobSchedule;
//import com.vsp.endpointinsightsapi.testrun.TestRun;

@Getter
@Setter
@Entity
@Table(name = "test_batch")
public class TestBatchTable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "batch_id")
    private Long batchId;

//    @ManyToOne
//    @JoinColumn(name = "schedule_id", nullable = false)
//    private JobSchedule jobSchedule;

//    @OneToMany(mappedBy = "testBatch", cascade = CascadeType.ALL)
//    private List<TestRun> testRuns;

    @Column(name = "created_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt = new Date();

    @Column(name = "status")
    private String status;

    @Column(name = "notes")
    private String notes;
}
