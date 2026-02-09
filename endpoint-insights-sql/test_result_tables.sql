create table test_result
(
    result_id uuid default gen_random_uuid()
        constraint test_result_pk
            primary key,
    job_type  integer
);

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
    run_id      uuid default gen_random_uuid()
        constraint test_run_pk
            primary key,
    job_id      uuid not null,
    run_by      text not null,
    status      text not null,
    started_at  timestamptz,
    finished_at timestamptz
);

