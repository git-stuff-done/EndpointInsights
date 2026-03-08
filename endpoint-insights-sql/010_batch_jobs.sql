CREATE TABLE IF NOT EXISTS batch_jobs (
    batch_id UUID NOT NULL,
    job_id UUID NOT NULL,
    CONSTRAINT batch_jobs_pk
        PRIMARY KEY (batch_id, job_id),
    CONSTRAINT fk_batch_jobs_batch
        FOREIGN KEY (batch_id)
        REFERENCES test_batch (id),
    CONSTRAINT fk_batch_jobs_job
        FOREIGN KEY (job_id)
        REFERENCES job (job_id)
);
