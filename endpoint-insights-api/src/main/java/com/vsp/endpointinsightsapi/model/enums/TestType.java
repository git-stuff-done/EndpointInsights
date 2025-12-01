package com.vsp.endpointinsightsapi.model.enums;

public enum TestType {
    PERF(0), INTEGRATION(1), E2E(2);

    private final Integer intValue;

    TestType(Integer intValue) {
        this.intValue = intValue;
    }

    public Integer toInteger() {
        return intValue;
    }
}
