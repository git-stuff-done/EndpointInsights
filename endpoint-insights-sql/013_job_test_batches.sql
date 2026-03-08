CREATE TABLE IF NOT EXISTS job_test_batches (
    job_job_id UUID NOT NULL,
    test_batches_id UUID NOT NULL,
    CONSTRAINT job_test_batches_pkey PRIMARY KEY (job_job_id, test_batches_id),
    CONSTRAINT fk2vle8iw6fm43no33ml63svxj5
        FOREIGN KEY (job_job_id)
        REFERENCES job (job_id),
    CONSTRAINT fk9u9pgq99t7rnx6n1byjr36t7m
        FOREIGN KEY (test_batches_id)
        REFERENCES test_batch (id)
);
