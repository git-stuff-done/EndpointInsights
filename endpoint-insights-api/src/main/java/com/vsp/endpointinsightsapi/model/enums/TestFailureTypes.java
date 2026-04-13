package com.vsp.endpointinsightsapi.model.enums;

public enum TestFailureTypes {
    LATENCY_THRESHOLD_EXCEEDED("Latency threshold exceeded"),
    OTHER("Other");

    private final String description;
    TestFailureTypes(String s) {
        this.description = s;
    }

    @Override
    public String toString() {
        return description;
    }
}
