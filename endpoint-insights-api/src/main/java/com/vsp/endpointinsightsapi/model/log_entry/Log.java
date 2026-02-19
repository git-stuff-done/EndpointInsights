package com.vsp.endpointinsightsapi.model.log_entry;

import com.vsp.endpointinsightsapi.model.AuditingEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Log extends AuditingEntity {

    @Column(name = "event_type")
    private String eventType;

    @Column(name = "details")
    private String details;

    @Column(name = "status")
    private String status;
}
