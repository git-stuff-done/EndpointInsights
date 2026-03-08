CREATE TABLE IF NOT EXISTS perf_test_result_code (
    result_id UUID NOT NULL,
    error_code INTEGER NOT NULL,
    sampler_name TEXT NOT NULL,
    thread_group TEXT NOT NULL,
    count INTEGER,
    CONSTRAINT perf_test_result_code_pk
        PRIMARY KEY (result_id, error_code, sampler_name, thread_group),
    CONSTRAINT perf_test_result_code_fk
        FOREIGN KEY (result_id, sampler_name, thread_group)
        REFERENCES perf_test_result (result_id, sampler_name, thread_group)
        ON DELETE CASCADE
);
