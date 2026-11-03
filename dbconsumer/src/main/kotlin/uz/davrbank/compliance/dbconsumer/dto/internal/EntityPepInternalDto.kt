package uz.davrbank.compliance.dbconsumer.dto.internal

import uz.davrbank.compliance.dbconsumer.domain.postgresql.EntityPEP
import uz.davrbank.compliance.dbconsumer.domain.postgresql.EntityPEPSubcategory

data class EntityPepInternalDto(
    val entityPep: EntityPEP,
    val entityPEPSubcategory: EntityPEPSubcategory
)
