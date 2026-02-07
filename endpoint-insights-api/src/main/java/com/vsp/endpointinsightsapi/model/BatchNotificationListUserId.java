package com.vsp.endpointinsightsapi.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "notification_list_user_ids")
public class BatchNotificationListUserId {

    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @Column(name = "batch_id")
    private UUID batchId;

    @Column(name = "user_id")
    private UUID userId;
}
