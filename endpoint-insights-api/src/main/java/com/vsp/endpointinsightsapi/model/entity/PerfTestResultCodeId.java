package com.vsp.endpointinsightsapi.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.Hibernate;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Getter
@Setter
@Embeddable
public class PerfTestResultCodeId implements Serializable {
	@Serial
	private static final long serialVersionUID = 3984884292218826468L;
	@NotNull
	@Column(name = "result_id", nullable = false)
	private UUID resultId;

	@NotNull
	@Column(name = "error_code", nullable = false)
	private Integer errorCode;

	@NotNull
	@Column(name = "sampler_name", nullable = false)
	private String samplerName;

	@NotNull
	@Column(name = "thread_group", nullable = false)
	private String threadGroup;

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
		PerfTestResultCodeId entity = (PerfTestResultCodeId) o;
		return Objects.equals(this.resultId, entity.resultId) &&
				Objects.equals(this.errorCode, entity.errorCode) &&
				Objects.equals(this.samplerName, entity.samplerName) &&
				Objects.equals(this.threadGroup, entity.threadGroup);
	}

	@Override
	public int hashCode() {
		return Objects.hash(resultId, errorCode, samplerName, threadGroup);
	}

}