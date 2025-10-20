package com.vsp.endpointinsightsapi.schedule;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.util.Date;

@Getter
@Setter
@Entity
@Table(name = "job_schedule")
public class JobSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long scheduleId;

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "job_id", nullable = false)
//    private Job job;

    @Column(name = "cron_expr", nullable = false, length = 50)
    private String cronExpr;

    @Column(name = "timezone", nullable = false, length = 50)
    private String timezone = "PDT";

    @Column(name = "next_run_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date nextRunAt;

    @Column(name = "is_enabled", nullable = false)
    private Boolean isEnabled = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    @Column(name = "updated_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = new Date();
        updatedAt = new Date();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = new Date();
    }
}