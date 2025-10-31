package com.vsp.endpointinsightsapi.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.Hibernate;

import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
@Embeddable
public class PerfTestResultCodeId implements Serializable {
	private static final long serialVersionUID = -2989881077012165282L;
	@NotNull
	@Column(name = "result_id", nullable = false)
	private Long resultId;

	@NotNull
	@Column(name = "error_code", nullable = false)
	private Integer errorCode;

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
		PerfTestResultCodeId entity = (PerfTestResultCodeId) o;
		return Objects.equals(this.resultId, entity.resultId) &&
				Objects.equals(this.errorCode, entity.errorCode);
	}

	@Override
	public int hashCode() {
		return Objects.hash(resultId, errorCode);
	}

}