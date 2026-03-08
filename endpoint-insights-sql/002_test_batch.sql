CREATE TABLE IF NOT EXISTS test_batch (
    active BOOLEAN,
    last_time_run TIMESTAMP(6),
    start_time TIMESTAMP(6),
    created_date TIMESTAMPTZ,
    schedule_id BIGINT,
    updated_date TIMESTAMPTZ NOT NULL,
    id UUID NOT NULL,
    batch_name VARCHAR(255) NOT NULL,
    created_by VARCHAR(255),
    updated_by VARCHAR(255) NOT NULL,
    cron_expression VARCHAR(255),
    CONSTRAINT test_batch_pkey PRIMARY KEY (id)
);
