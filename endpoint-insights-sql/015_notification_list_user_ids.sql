CREATE TABLE IF NOT EXISTS notification_list_user_ids (
    id UUID NOT NULL,
    batch_id UUID,
    user_id UUID,
    CONSTRAINT notification_list_user_ids_pkey PRIMARY KEY (id)
);

CREATE SEQUENCE IF NOT EXISTS notification_list_user_ids_seq
    START WITH 1
    INCREMENT BY 50
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
