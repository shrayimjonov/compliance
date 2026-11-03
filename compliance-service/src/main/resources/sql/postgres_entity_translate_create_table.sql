CREATE TABLE IF NOT EXISTS entity_translate (
    id UUID PRIMARY KEY,
    entity_id UUID NOT NULL,
    entity_type VARCHAR NOT NULL,
    last_updated_date TIMESTAMP NOT NULL,
    original_text TEXT NOT NULL,
    translations JSONB NOT NULL DEFAULT '{}'::jsonb
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_entity_translate_entity_type_id
    ON entity_translate (entity_type, entity_id);
