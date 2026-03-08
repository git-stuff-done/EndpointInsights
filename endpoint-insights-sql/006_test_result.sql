CREATE TABLE IF NOT EXISTS test_result (
    job_type INTEGER,
    result_id UUID DEFAULT gen_random_uuid() NOT NULL,
    run_id UUID NOT NULL,
    CONSTRAINT test_result_pkey PRIMARY KEY (result_id)
);
