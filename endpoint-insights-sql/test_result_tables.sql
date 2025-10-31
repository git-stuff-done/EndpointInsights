create table test_result
(
    result_id integer generated always as identity
        constraint result_id_pk
            primary key,
    job_type  integer
);

create table perf_test_result
(
    result_id             integer not null
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
    result_id  bigint  not null
        constraint perf_test_result_code_fk
            references perf_test_result,
    error_code integer not null,
    count      integer,
    constraint perf_test_result_code_pk
        primary key (result_id, error_code)
);

