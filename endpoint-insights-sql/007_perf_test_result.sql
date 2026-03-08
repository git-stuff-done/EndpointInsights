CREATE TABLE IF NOT EXISTS perf_test_result (
    error_rate_percent DOUBLE PRECISION,
    p50_latency_ms INTEGER,
    p95_latency_ms INTEGER,
    p99_latency_ms INTEGER,
    volume_last_5_minutes INTEGER,
    volume_last_minute INTEGER,
    result_id UUID NOT NULL,
    sampler_name VARCHAR(255) NOT NULL,
    thread_group VARCHAR(255) NOT NULL,
    CONSTRAINT perf_test_result_pkey PRIMARY KEY (result_id),
    CONSTRAINT fki3moc1sg6bo425u35rba5w645
        FOREIGN KEY (result_id)
        REFERENCES test_result (result_id)
);
