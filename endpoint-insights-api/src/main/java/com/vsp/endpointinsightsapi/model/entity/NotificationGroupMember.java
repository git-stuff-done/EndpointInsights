package com.vsp.endpointinsightsapi.model.entity;

import com.vsp.endpointinsightsapi.model.NotificationGroup;
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
@Table(name = "notification_group_members", uniqueConstraints = {
    @UniqueConstraint(name = "notification_group_members_group_id_email_key", columnNames = {"group_id", "email"})
})
public class NotificationGroupMember {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "group_id", nullable = false)
    private UUID groupId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", insertable = false, updatable = false)
    private NotificationGroup group;

    @Column(name = "email", nullable = false, length = 320)
    private String email;
}
