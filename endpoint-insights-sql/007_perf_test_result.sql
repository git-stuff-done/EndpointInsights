CREATE TABLE IF NOT EXISTS perf_test_result (
    result_id UUID NOT NULL,
    sampler_name TEXT NOT NULL,
    thread_group TEXT NOT NULL,
    p50_latency_ms INTEGER,
    p95_latency_ms INTEGER,
    p99_latency_ms INTEGER,
    volume_last_minute INTEGER,
    volume_last_5_minutes INTEGER,
    error_rate_percent DOUBLE PRECISION,
    CONSTRAINT perf_test_result_pk
        PRIMARY KEY (result_id, sampler_name, thread_group),
    CONSTRAINT perf_test_result_fk
        FOREIGN KEY (result_id)
        REFERENCES test_result (result_id)
        ON DELETE CASCADE
);
