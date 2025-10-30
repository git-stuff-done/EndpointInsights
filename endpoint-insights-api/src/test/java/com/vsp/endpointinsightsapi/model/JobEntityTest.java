package com.vsp.endpointinsightsapi.model;

import com.vsp.endpointinsightsapi.model.enums.JobStatus;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class JobEntityTest {
    @Test
    void shouldSetTimestampsOnCreate() {
        Job job = new Job();
        job.onCreate();

        assertThat(job.getCreatedAt()).isNotNull();
        assertThat(job.getUpdatedAt()).isNotNull();
    }

    @Test
    void shouldUpdateTimestampOnUpdate() {
        Job job = new Job();
        job.onCreate();

        Date initialTime = new Date(System.currentTimeMillis() - 1000);
        job.setUpdatedAt(initialTime);

        job.onUpdate();

        assertThat(job.getUpdatedAt()).isAfter(initialTime);
    }

    @Test
    void shouldDefaultToPendingStatus() {
        Job job = new Job();
        assertThat(job.getStatus()).isEqualTo(JobStatus.PENDING);
    }

}
