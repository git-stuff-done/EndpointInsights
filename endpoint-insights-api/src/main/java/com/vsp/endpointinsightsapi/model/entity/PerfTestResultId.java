package com.vsp.endpointinsightsapi.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Getter
@Setter
@Embeddable
public class PerfTestResultId implements Serializable {

    @Column(name = "result_id", nullable = false)
    private UUID resultId;

    @Column(name = "sampler_name", nullable = false)
    private String samplerName;

    @Column(name = "thread_group", nullable = false)
    private String threadGroup;

    public PerfTestResultId() {
    }

    public PerfTestResultId(UUID resultId, String samplerName, String threadGroup) {
        this.resultId = resultId;
        this.samplerName = samplerName;
        this.threadGroup = threadGroup;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PerfTestResultId that)) return false;
        return Objects.equals(resultId, that.resultId) &&
                Objects.equals(samplerName, that.samplerName) &&
                Objects.equals(threadGroup, that.threadGroup);
    }

    @Override
    public int hashCode() {
        return Objects.hash(resultId, samplerName, threadGroup);
    }
}
