CREATE TABLE IF NOT EXISTS job (
    job_id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    name TEXT NOT NULL,
    description TEXT,
    git_url TEXT,
    git_auth_type VARCHAR(20),
    git_username TEXT,
    git_password TEXT,
    git_ssh_private_key TEXT,
    git_ssh_passphrase TEXT,
    run_command TEXT,
    compile_command TEXT,
    test_type VARCHAR(20) NOT NULL,
    config JSONB,
    created_by TEXT,
    created_date TIMESTAMPTZ,
    updated_by TEXT NOT NULL,
    updated_date TIMESTAMPTZ NOT NULL
);
