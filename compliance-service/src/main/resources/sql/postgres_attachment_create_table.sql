CREATE TABLE IF NOT EXISTS attachment (
    id              UUID PRIMARY KEY,
    bucket_name     VARCHAR(255) NOT NULL,
    object_name     VARCHAR(512) NOT NULL,
    original_name   VARCHAR(512),
    attachment_url  VARCHAR(1024) NOT NULL,
    content_type    VARCHAR(32) NOT NULL,
    status          VARCHAR(16) NOT NULL DEFAULT 'UPLOADING',
    entity_id       UUID,
    entity_type     VARCHAR(64),
    created_date    TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_attachment_entity
    ON attachment (entity_id, entity_type, status);

CREATE INDEX IF NOT EXISTS idx_attachment_status
    ON attachment (status);
