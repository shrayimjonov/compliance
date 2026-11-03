package uz.davrbank.compliance.dbconsumer.dto.internal

import uz.davrbank.compliance.dbconsumer.domain.postgresql.EntitySOE
import uz.davrbank.compliance.dbconsumer.domain.postgresql.EntitySOEDomain
import uz.davrbank.compliance.dbconsumer.domain.postgresql.EntitySOESubcategory

data class EntitySOEsInternalDto(
    var entitySOE: EntitySOE,
    var entitySOESubcategory: List<EntitySOESubcategory>,
    var entitySOEDomain: List<EntitySOEDomain>
)
