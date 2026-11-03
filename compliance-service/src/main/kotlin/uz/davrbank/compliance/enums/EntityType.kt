package uz.davrbank.compliance.enums

import com.fasterxml.jackson.annotation.JsonValue

enum class EntityType(@get:JsonValue val value: String) {
    COMPLIANCE("compliance"),
    ENTITY_DOB("entity_dob"),
    ENTITY_ADDRESS("entity_address"),
    ENTITY_ALIAS("entity_alias"),
    ENTITY_IDENTIFICATION("entity_identification"),
    ENTITY_COUNTRY_ASSOCIATION("entity_country_association"),
    ENTITY_POSITION("entity_position"),
    ENTITY_RELATIONSHIP("entity_relationship"),
    ENTITY_REMARK("entity_remark"),
    ENTITY_SOURCE_ITEM("entity_source_item"),
    ENTITY_SEGMENT("entity_segment"),
    ENTITY_PEP("entity_pep"),
    ENTITY_PEP_SUBCATEGORY("entity_pep_subcategory"),
    ENTITY_ADVERSE_MEDIA("entity_adverse_media"),
    ENTITY_ADVERSE_MEDIA_SUBCATEGORY("entity_adverse_media_subcategory"),
    ENTITY_ENFORCEMENT("entity_enforcement"),
    ENTITY_ENFORCEMENT_SUBCATEGORY("entity_enforcement_subcategory"),
    ENTITY_SANCTION("entity_sanction"),
    ENTITY_SOE("entity_soe"),
    ENTITY_SOE_SUBCATEGORY("entity_soe_subcategory"),
    ENTITY_SOE_DOMAIN("entity_soe_domain"),
    ADDITIONAL_SEGMENT("additional_segment"),
    RECORD("record"),
    FIELD("field");

    companion object {
        fun fromValueOrNull(value: String?): EntityType? {
            val normalized = value?.trim()
                ?.takeIf(String::isNotBlank)
                ?.replace('-', '_')
                ?: return null
            val compact = normalized.replace("_", "")

            return entries.firstOrNull { entityType ->
                entityType.value.equals(normalized, ignoreCase = true) ||
                    entityType.name.equals(normalized, ignoreCase = true) ||
                    entityType.value.replace("_", "").equals(compact, ignoreCase = true) ||
                    entityType.name.replace("_", "").equals(compact, ignoreCase = true)
            }
        }
    }
}
