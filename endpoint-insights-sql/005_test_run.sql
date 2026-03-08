CREATE TABLE IF NOT EXISTS test_run (
    run_id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    job_id UUID NOT NULL,
    result_id UUID,
    batch_id UUID,
    run_by TEXT NOT NULL,
    status TEXT NOT NULL,
    started_at TIMESTAMPTZ,
    finished_at TIMESTAMPTZ,
    CONSTRAINT fk_test_run_batch
        FOREIGN KEY (batch_id)
        REFERENCES test_batch (id)
        ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS test_run_batch_id_idx
    ON test_run (batch_id);
