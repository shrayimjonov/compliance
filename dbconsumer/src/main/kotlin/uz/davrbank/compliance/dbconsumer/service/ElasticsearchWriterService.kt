package uz.davrbank.compliance.dbconsumer.service

import uz.davrbank.compliance.dbconsumer.domain.elasticsearch.NewComplianceData

interface ElasticsearchWriterService {
    fun save(document: NewComplianceData)
    fun delete(ids: List<String>)
    fun deleteAllData()
}

