package com.vsp.endpointinsightsapi.testbatchtable;

import jakarta.persistence.*;
import java.time.LocalDateTime;

//import com.vsp.endpointinsightsapi.schedule.JobSchedule;
//import com.vsp.endpointinsightsapi.testrun.TestRun;

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
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "status")
    private String status;

    @Column(name = "notes")
    private String notes;

    // --- Getters & Setters ---

    public Long getBatchId() {
        return batchId;
    }

    public void setBatchId(Long batchId) {
        this.batchId = batchId;
    }

//    public JobSchedule getJobSchedule() {
//        return jobSchedule;
//    }

//    public void setJobSchedule(JobSchedule jobSchedule) {
//        this.jobSchedule = jobSchedule;
//    }

//    public List<TestRun> getTestRuns() {
//        return testRuns;
//    }

//    public void setTestRuns(List<TestRun> testRuns) {
//        this.testRuns = testRuns;
//    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
