CREATE TABLE IF NOT EXISTS schedule_run (
    schedule_id VARCHAR(255) NOT NULL,
    created_by VARCHAR(255),
    created_date TIMESTAMPTZ,
    updated_by VARCHAR(255) NOT NULL,
    updated_date TIMESTAMPTZ NOT NULL,
    start_time TIMESTAMP(6),
    status VARCHAR(255),
    CONSTRAINT schedule_run_pkey PRIMARY KEY (schedule_id)
);
