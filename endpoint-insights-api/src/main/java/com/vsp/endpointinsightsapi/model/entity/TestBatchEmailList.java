package com.vsp.endpointinsightsapi.model.entity;


import com.vsp.endpointinsightsapi.model.TestBatch;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "test_batch_email_lists")
public class TestBatchEmailList {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "batch_id", nullable = false)
    private UUID batchId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_id", nullable = false, insertable = false, updatable = false)
    private TestBatch testBatch;

    @Column(name = "email", nullable = false, length = 320)
    private String email;
}
