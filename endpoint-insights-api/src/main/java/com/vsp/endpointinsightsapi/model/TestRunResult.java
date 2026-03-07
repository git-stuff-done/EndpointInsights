package com.vsp.endpointinsightsapi.model;

import java.util.UUID;

public record TestRunResult(boolean passed, UUID resultId) {
}
