CREATE TABLE IF NOT EXISTS log_entry_info (
    id UUID NOT NULL,
    user_id UUID,
    job_id UUID,
    event_type VARCHAR(255) NOT NULL,
    details VARCHAR(255),
    created_by VARCHAR(255),
    created_date TIMESTAMP,
    updated_by VARCHAR(255),
    updated_date TIMESTAMP,
    status VARCHAR(255),
    CONSTRAINT log_entry_info_pkey PRIMARY KEY (id)
);
