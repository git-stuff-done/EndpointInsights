CREATE TABLE IF NOT EXISTS test_batch (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    batch_name TEXT NOT NULL,
    schedule_id BIGINT,
    start_time TIMESTAMP,
    last_time_run TIMESTAMP,
    active BOOLEAN,
    cron_expression TEXT,
    created_by TEXT,
    created_date TIMESTAMPTZ,
    updated_by TEXT NOT NULL,
    updated_date TIMESTAMPTZ NOT NULL
);
