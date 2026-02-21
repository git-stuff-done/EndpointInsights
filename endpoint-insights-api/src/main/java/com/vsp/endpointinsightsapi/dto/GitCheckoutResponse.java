package com.vsp.endpointinsightsapi.dto;

import java.nio.file.Path;
import java.util.UUID;

public class GitCheckoutResponse {
    private final UUID jobId;
    private final String checkoutPath;

    public GitCheckoutResponse(UUID jobId, Path checkoutPath) {
        this.jobId = jobId;
        this.checkoutPath = checkoutPath == null ? null : checkoutPath.toString();
    }

    public UUID getJobId() {
        return jobId;
    }

    public String getCheckoutPath() {
        return checkoutPath;
    }
}
