package com.vsp.endpointinsightsapi.dto;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class GitCheckoutResponseTest {

    @Test
    void constructor_withPath_setsFields() {
        UUID jobId = UUID.randomUUID();
        Path path = Path.of("/tmp/checkout");

        GitCheckoutResponse response = new GitCheckoutResponse(jobId, path);

        assertEquals(jobId, response.getJobId());
        assertEquals(path.toString(), response.getCheckoutPath());
    }

    @Test
    void constructor_withNullPath_setsNullCheckoutPath() {
        UUID jobId = UUID.randomUUID();

        GitCheckoutResponse response = new GitCheckoutResponse(jobId, null);

        assertEquals(jobId, response.getJobId());
        assertNull(response.getCheckoutPath());
    }
}
