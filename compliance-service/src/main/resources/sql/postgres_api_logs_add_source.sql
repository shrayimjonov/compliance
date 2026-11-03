ALTER TABLE api_logs
    ADD COLUMN IF NOT EXISTS source VARCHAR;

UPDATE api_logs
SET source = 'SMARTBAS'
WHERE source IS NULL;

ALTER TABLE api_logs
    ALTER COLUMN source SET DEFAULT 'SMARTBAS',
    ALTER COLUMN source SET NOT NULL;

CREATE INDEX IF NOT EXISTS idx_api_logs_source_created_at_desc
    ON "api_logs" ("source", "created_at" DESC);

alter table api_logs add column if not exists username varchar;
