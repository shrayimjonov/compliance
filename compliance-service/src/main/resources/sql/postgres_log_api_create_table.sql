CREATE TABLE IF NOT EXISTS api_logs (
    id UUID PRIMARY KEY,
    request_id TEXT NOT NULL,
    method VARCHAR(16) NOT NULL,
    endpoint TEXT NOT NULL,
    query_params TEXT,
    request_body JSONB,
    response_body JSONB,
    additional JSONB,
    source VARCHAR(16) NOT NULL DEFAULT 'NCI',
    status_code INTEGER NOT NULL,
    time_taken_ms BIGINT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL
);


alter table api_logs add column username varchar;
