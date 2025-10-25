/**
 * Overall test result category.
 *  - PASS: thresholds all satisfied
 *  - WARN: performance near limits
 *  - FAIL: threshold breached
 *  - UNKNOWN: not enough data / not yet run
 */
export type TestStatus = 'PASS' | 'WARN' | 'FAIL' | 'UNKNOWN';

/** HTTP status code summary for a given test run. */
export interface HttpBreakdown {
    /** e.g. 200, 401, 500 */
    code: number;
    /** how many responses returned with that code */
    count: number;
}

/**
 * Warning and failure thresholds for different metrics.
 * Used to color-code results and determine status.
 */
export interface ThresholdConfig {
    latencyMs?:    { warn: number; fail: number };
    errorRatePct?: { warn: number; fail: number };
    volumePerMin?: { warn: number; fail: number };
}

/**
 * Represents one monitored API or system test result.
 */
export interface TestRecord {
    /** Unique ID used for routing / keys */
    id: string;
    /** Human-readable name (e.g., “Login API”) */
    name: string;
    /** Optional short description of what the test checks */
    description?: string;

    /** Current computed status (PASS/WARN/FAIL/UNKNOWN) */
    status: TestStatus;

    /**
     * ISO-8601 timestamp of the most recent run.
     * Example: "2025-10-19T22:14:00Z"
     * Stored as a string so the client can easily format
     * it in the user’s local time zone.
     */
    lastRunIso?: string;

    /** Median latency (p50), in milliseconds */
    latencyMsP50?: number;
    /** 95th percentile latency, in milliseconds */
    latencyMsP95?: number;
    /** 99th percentile latency, in milliseconds */
    latencyMsP99?: number;

    /**
     * Requests handled in the last 1 minute window.
     * Gives a quick read on current traffic volume.
     */
    volume1m?: number;

    /**
     * Requests handled in the last 5 minute window.
     * Useful for spotting short-term spikes or drops.
     */
    volume5m?: number;

    /** Breakdown of HTTP responses by status code */
    httpBreakdown?: HttpBreakdown[];

    /**
     * Percentage of total requests that failed
     * (non-2xx responses or test errors).
     * Expressed as a raw percentage (e.g., 1.2 for 1.2%).
     */
    errorRatePct?: number;

    /** Threshold configuration used to evaluate PASS/WARN/FAIL */
    thresholds?: ThresholdConfig;
}

