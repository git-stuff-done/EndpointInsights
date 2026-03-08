CREATE TABLE IF NOT EXISTS test_result (
    result_id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    run_id UUID NOT NULL,
    job_type INTEGER,
    CONSTRAINT fk_test_result_run
        FOREIGN KEY (run_id)
        REFERENCES test_run (run_id)
        ON DELETE CASCADE
);

CREATE UNIQUE INDEX IF NOT EXISTS test_result_run_id_uq
    ON test_result (run_id);
