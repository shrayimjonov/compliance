
CREATE EXTENSION IF NOT EXISTS pg_trgm;

CREATE INDEX IF NOT EXISTS idx_compliance_type_id_desc
    ON "compliance" ("type", "id" DESC);

CREATE INDEX IF NOT EXISTS idx_compliance_dtype_id_desc
    ON "compliance" ("data_type", "id" DESC);

CREATE INDEX IF NOT EXISTS idx_compliance_dtype_ctype_type_id_desc
    ON "compliance" ("data_type", "compliance_type", "type", "id" DESC);

CREATE INDEX IF NOT EXISTS idx_compliance_name_trgm
    ON "compliance" USING gin (lower("name") gin_trgm_ops);

CREATE INDEX IF NOT EXISTS idx_compliance_first_name_trgm
    ON "compliance" USING gin (lower("first_name") gin_trgm_ops);

CREATE INDEX IF NOT EXISTS idx_compliance_last_name_trgm
    ON "compliance" USING gin (lower("last_name") gin_trgm_ops);

CREATE INDEX IF NOT EXISTS idx_compliance_full_name_trgm
    ON "compliance" USING gin (lower("full_name") gin_trgm_ops);

CREATE INDEX IF NOT EXISTS idx_entitydob_compliance_id_id
    ON "entitydob" ("compliance_id", "id");

CREATE INDEX IF NOT EXISTS idx_entity_address_compliance_id_id
    ON "entity_address" ("compliance_id", "id");

CREATE INDEX IF NOT EXISTS idx_entity_alias_compliance_id_id
    ON "entity_alias" ("compliance_id", "id");

CREATE INDEX IF NOT EXISTS idx_entity_identification_compliance_id_id
    ON "entity_identification" ("compliance_id", "id");

CREATE INDEX IF NOT EXISTS idx_entity_country_assoc_compliance_id_id
    ON "entity_country_association" ("compliance_id", "id");

CREATE INDEX IF NOT EXISTS idx_entity_position_compliance_id_id
    ON "entity_position" ("compliance_id", "id");

CREATE INDEX IF NOT EXISTS idx_entity_relationship_compliance_id_id
    ON "entity_relationship" ("compliance_id", "id");

CREATE INDEX IF NOT EXISTS idx_entity_remark_compliance_id_id
    ON "entity_remark" ("compliance_id", "id");

CREATE INDEX IF NOT EXISTS idx_entity_source_item_compliance_id_id
    ON "entity_source_item" ("compliance_id", "id");

CREATE INDEX IF NOT EXISTS idx_entitypep_compliance_id_id
    ON "entitypep" ("compliance_id", "id");

CREATE INDEX IF NOT EXISTS idx_entitypepsubcat_compliance_id_id
    ON "entitypepsubcategory" ("compliance_id", "id");

CREATE INDEX IF NOT EXISTS idx_entity_adverse_media_compliance_id_id
    ON "entity_adverse_media" ("compliance_id", "id");

CREATE INDEX IF NOT EXISTS idx_entity_adv_media_subcat_compliance_id_id
    ON "entity_adverse_media_subcategory" ("compliance_id", "id");

CREATE INDEX IF NOT EXISTS idx_entity_enforcement_compliance_id_id
    ON "entity_enforcement" ("compliance_id", "id");

CREATE INDEX IF NOT EXISTS idx_entity_enf_subcat_compliance_id_id
    ON "entity_enforcement_subcategory" ("compliance_id", "id");

CREATE INDEX IF NOT EXISTS idx_entity_sanction_compliance_id_id
    ON "entity_sanction" ("compliance_id", "id");

CREATE INDEX IF NOT EXISTS idx_entitysoe_compliance_id_id
    ON "entitysoe" ("compliance_id", "id");

CREATE INDEX IF NOT EXISTS idx_entitysoesubcat_compliance_id_id
    ON "entitysoesubcategory" ("compliance_id", "id");

CREATE INDEX IF NOT EXISTS idx_entitysoedomain_compliance_id_id
    ON "entitysoedomain" ("compliance_id", "id");

CREATE INDEX IF NOT EXISTS idx_record_compliance_id_id
    ON "record" ("compliance_id", "id");

CREATE INDEX IF NOT EXISTS idx_field_compliance_id_id
    ON "field" ("compliance_id", "id");

CREATE INDEX IF NOT EXISTS idx_entity_segment_active_compliance_id_id_desc
    ON "entity_segment" ("compliance_id", "id" DESC)
    WHERE "deleted" = false;

CREATE INDEX IF NOT EXISTS idx_additional_segment_active_compliance_id_id
    ON "additional_segment" ("compliance_id", "id")
    WHERE "deleted" = false;

CREATE INDEX IF NOT EXISTS idx_entitypepsubcat_entity_pep_id_id
    ON "entitypepsubcategory" ("entity_pep_id", "id");

CREATE INDEX IF NOT EXISTS idx_entity_adv_media_subcat_parent_id_id
    ON "entity_adverse_media_subcategory" ("entity_adverse_media_id", "id");

CREATE INDEX IF NOT EXISTS idx_entity_enf_subcat_parent_id_id
    ON "entity_enforcement_subcategory" ("entity_enforcement_id", "id");

CREATE INDEX IF NOT EXISTS idx_entitysoesubcat_entitysoe_id_id
    ON "entitysoesubcategory" ("entitysoe_id", "id");

CREATE INDEX IF NOT EXISTS idx_entitysoedomain_entitysoe_id_id
    ON "entitysoedomain" ("entitysoe_id", "id");

CREATE INDEX IF NOT EXISTS idx_record_additional_segment_id_id
    ON "record" ("additional_segment_id", "id");

CREATE INDEX IF NOT EXISTS idx_field_record_id_id
    ON "field" ("record_id", "id");

CREATE INDEX IF NOT EXISTS idx_api_logs_created_at_desc
    ON "api_logs" ("created_at" DESC);

CREATE INDEX IF NOT EXISTS idx_api_logs_method_created_at_desc
    ON "api_logs" ("method", "created_at" DESC);

CREATE INDEX IF NOT EXISTS idx_api_logs_status_code_created_at_desc
    ON "api_logs" ("status_code", "created_at" DESC);

CREATE INDEX IF NOT EXISTS idx_api_logs_method_status_code_created_at_desc
    ON "api_logs" ("method", "status_code", "created_at" DESC);

CREATE INDEX IF NOT EXISTS idx_api_logs_request_id_trgm
    ON "api_logs" USING gin (lower("request_id") gin_trgm_ops);

CREATE INDEX IF NOT EXISTS idx_api_logs_username_trgm
    ON "api_logs" USING gin (lower("username") gin_trgm_ops);

CREATE INDEX IF NOT EXISTS idx_api_logs_endpoint_trgm
    ON "api_logs" USING gin (lower("endpoint") gin_trgm_ops);
