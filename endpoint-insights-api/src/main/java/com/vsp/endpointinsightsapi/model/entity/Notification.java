package com.vsp.endpointinsightsapi.model.entity;

import java.util.UUID;

import org.hibernate.annotations.UuidGenerator;

import com.vsp.endpointinsightsapi.model.TestBatch;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "notification_list_user_ids")
public class Notification {

	@Id
	@GeneratedValue
	@UuidGenerator
	@Column(name = "id")
	private UUID id;

	@Column(name = "batch_id")
	private UUID batchId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "batch_id", insertable = false, updatable = false)
	private TestBatch testBatch;

	@Column(name = "user_id")
	private UUID userId;
}
