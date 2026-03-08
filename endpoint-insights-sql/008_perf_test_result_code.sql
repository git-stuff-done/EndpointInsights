CREATE TABLE IF NOT EXISTS perf_test_result_code (
    count INTEGER,
    error_code INTEGER NOT NULL,
    result_id UUID NOT NULL,
    sampler_name VARCHAR(255) NOT NULL,
    thread_group VARCHAR(255) NOT NULL,
    CONSTRAINT perf_test_result_code_pkey PRIMARY KEY (error_code, result_id),
    CONSTRAINT fknh7d2grhr3ids3wdb4es59xhp
        FOREIGN KEY (result_id)
        REFERENCES perf_test_result (result_id)
);
