DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_type
        WHERE typname = 'mailing_membership_status'
    ) THEN
        CREATE TYPE mailing_membership_status AS ENUM ('ACTIVE', 'UNSUBSCRIBED', 'BOUNCED');
    END IF;
END$$;

CREATE TABLE IF NOT EXISTS mailing_list_recipient (
    mailing_list_id BIGINT NOT NULL,
    mailing_recipient_id BIGINT NOT NULL,
    status mailing_membership_status NOT NULL DEFAULT 'ACTIVE',
    added_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    added_by TEXT,
    removed_at TIMESTAMPTZ,
    removed_by TEXT,
    CONSTRAINT mailing_list_recipient_pk
        PRIMARY KEY (mailing_list_id, mailing_recipient_id),
    CONSTRAINT fk_mlr_list
        FOREIGN KEY (mailing_list_id)
        REFERENCES mailing_list (mailing_list_id)
        ON DELETE CASCADE,
    CONSTRAINT fk_mlr_recipient
        FOREIGN KEY (mailing_recipient_id)
        REFERENCES mailing_recipient (mailing_recipient_id)
        ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS ix_mlr_recipient
    ON mailing_list_recipient (mailing_recipient_id);

CREATE INDEX IF NOT EXISTS ix_mlr_list_status
    ON mailing_list_recipient (mailing_list_id, status);
