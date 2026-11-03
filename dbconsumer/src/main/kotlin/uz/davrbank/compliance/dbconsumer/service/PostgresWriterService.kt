package uz.davrbank.compliance.dbconsumer.service

import uz.davrbank.compliance.dbconsumer.domain.postgresql.Compliance
import uz.davrbank.compliance.dbconsumer.dto.internal.AdditionalDataToEntityDto

interface PostgresWriterService {
    fun saveEntity(entity: Compliance): Compliance
    fun saveAdditionalData(request: AdditionalDataToEntityDto)
    fun deleteEntity(entityGuid: String)
}

