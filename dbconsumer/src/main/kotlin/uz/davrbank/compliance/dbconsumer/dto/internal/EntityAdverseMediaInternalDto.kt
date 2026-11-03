package uz.davrbank.compliance.dbconsumer.dto.internal

import uz.davrbank.compliance.dbconsumer.domain.postgresql.EntityAdverseMedia
import uz.davrbank.compliance.dbconsumer.domain.postgresql.EntityAdverseMediaSubcategory

data class EntityAdverseMediaInternalDto(
    val entityAdverseMedia: EntityAdverseMedia,
    val entityAdverseMediaSubcategory: List<EntityAdverseMediaSubcategory>
)
