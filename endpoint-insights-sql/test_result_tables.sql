create table test_result
(
    result_id uuid default gen_random_uuid() primary key,
    run_id    uuid not null references test_run(run_id) on delete cascade,
    job_type  integer
);

create unique index test_result_run_id_uq on test_result(run_id);

create table perf_test_result
(
    result_id             uuid not null
        constraint perf_test_result_pk
            primary key
        constraint perf_test_result_fk
            references test_result,
    p50_latency_ms        integer,
    p95_latency_ms        integer,
    p99_latency_ms        integer,
    volume_last_minute    integer,
    volume_last_5_minutes integer,
    error_rate_percent    double precision
);

create table perf_test_result_code
(
    result_id  uuid  not null
        constraint perf_test_result_code_fk
            references perf_test_result,
    error_code integer not null,
    count      integer,
    constraint perf_test_result_code_pk
        primary key (result_id, error_code)
);

create table test_run
(
    run_id      uuid default gen_random_uuid() primary key,
    job_id      uuid not null,
    batch_id    uuid references test_batch(id) on delete set null,
    run_by      text not null,
    status      text not null,
    started_at  timestamptz,
    finished_at timestamptz
);

create index test_run_batch_id_idx on test_run(batch_id);

