CREATE TABLE IF NOT EXISTS test_batch_tests (
    job_id UUID NOT NULL,
    test_job_id UUID NOT NULL,
    CONSTRAINT test_batch_tests_pkey PRIMARY KEY (job_id, test_job_id)
);
