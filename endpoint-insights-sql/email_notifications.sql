create table notifications
(
    notification_id     uuid                 default gen_random_uuid() primary key,

    run_id              uuid        not null references test_run (run_id) on delete cascade,
    batch_id            uuid        references test_batch (id) on delete set null,
    result_id           uuid        references test_result (result_id) on delete set null,

    notification_type   text        not null,
    channel             text        not null,
    recipient           text        not null,
    status              text        not null,
    sent_at             timestamptz not null default now(),

    provider_message_id text,
    error               text,

    idempotency_key     text        not null,
    created_at          timestamptz not null default now()
);

create unique index notifications_idempotency_uq on notifications (idempotency_key);
create index notifications_run_id_idx on notifications (run_id);