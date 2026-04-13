-- Create notification_groups table
CREATE TABLE IF NOT EXISTS notification_groups (
    id UUID DEFAULT gen_random_uuid() NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    created_by VARCHAR(255),
    created_date TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(255),
    updated_date TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT notification_groups_pkey PRIMARY KEY (id),
    CONSTRAINT notification_groups_name_key UNIQUE (name)
);

-- Create notification_group_members table
CREATE TABLE IF NOT EXISTS notification_group_members (
    id UUID DEFAULT gen_random_uuid() NOT NULL,
    group_id UUID NOT NULL,
    email VARCHAR(320) NOT NULL,
    CONSTRAINT notification_group_members_pkey PRIMARY KEY (id),
    CONSTRAINT fk_notification_group_members_group_id
        FOREIGN KEY (group_id)
        REFERENCES notification_groups (id) ON DELETE CASCADE,
    CONSTRAINT notification_group_members_group_id_email_key UNIQUE (group_id, email)
);

-- Create index for group_id lookup
CREATE INDEX IF NOT EXISTS idx_notification_group_members_group_id
    ON notification_group_members (group_id);

-- Add optional group_id column to test_batch_email_lists for future use
ALTER TABLE IF EXISTS notification_groups OWNER TO postgres;
ALTER TABLE IF EXISTS notification_group_members OWNER TO postgres;
