CREATE TABLE IF NOT EXISTS test_run (
    run_id UUID NOT NULL,
    started_at TIMESTAMPTZ,
    finished_at TIMESTAMPTZ,
    job_id UUID NOT NULL,
    run_by VARCHAR(255) NOT NULL,
    status VARCHAR(20) NOT NULL,
    result_id UUID,
    batch_id UUID,
    CONSTRAINT test_run_pkey PRIMARY KEY (run_id),
    CONSTRAINT test_run_status_check CHECK ((status::TEXT = ANY ((ARRAY['PENDING', 'RUNNING', 'COMPLETED', 'FAILED'])::TEXT[])))
);
