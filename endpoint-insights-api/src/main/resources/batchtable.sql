CREATE TABLE test_batch (

    batch_id SERIAL PRIMARY KEY,
    schedule_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(255),
    notes VARCHAR(255),

    CONSTRAINT fk_job_schedule
        FOREIGN KEY(schedule_id)
        REFERENCES job_schedule(schedule_id)
        ON DELETE CASCADE
);
