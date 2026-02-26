create table test_batch (
                id uuid default gen_random_uuid() primary key,
                created_by text not null,
                created_at timestamptz not null default now(),
                name text,
                status text not null default 'PENDING'
);