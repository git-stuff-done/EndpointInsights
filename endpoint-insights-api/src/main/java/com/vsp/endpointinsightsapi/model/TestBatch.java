package com.vsp.endpointinsightsapi.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.vsp.endpointinsightsapi.model.entity.TestBatchEmailList;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;


import java.time.LocalDateTime;
import java.util.*;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "test_batch")
public class TestBatch extends AuditingEntity{

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id", nullable = false)
    private UUID batchId;

   @JsonManagedReference
   @ManyToMany(fetch = FetchType.EAGER)
   @JoinTable(
           name = "batch_jobs",
           joinColumns = @JoinColumn(name = "batch_id", referencedColumnName = "id"),
           inverseJoinColumns = @JoinColumn(name = "job_id", referencedColumnName = "job_id")
   )
   private List<Job> jobs = new ArrayList<>();

    @Column(name = "batch_name", nullable = false)
    String batchName;

    @Column(name = "schedule_id")
    Long scheduleId;

    @Column(name = "start_time")
    LocalDateTime startTime;

    @Column(name = "last_time_run")
    LocalDateTime lastTimeRun;

    @Column(name = "active")
    Boolean active;

    @Column(name = "cron_expression")
    String cronExpression;

    @JsonManagedReference
    @ToString.Exclude
    @OneToMany(mappedBy = "testBatch", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    List<TestBatchEmailList> notificationList;
}
