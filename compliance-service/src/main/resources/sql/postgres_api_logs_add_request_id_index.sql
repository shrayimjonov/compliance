CREATE INDEX IF NOT EXISTS idx_api_logs_request_id_source_created_at_desc
    ON "api_logs" ("request_id", "source", "created_at" DESC);
