package com.vsp.endpointinsightsapi.model;

import com.vsp.endpointinsightsapi.model.enums.JobStatus;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class JobEntityTest {

    @Test
    void shouldDefaultToPendingStatus() {
        Job job = new Job();
        assertThat(job.getStatus()).isEqualTo(JobStatus.PENDING);
    }

}
