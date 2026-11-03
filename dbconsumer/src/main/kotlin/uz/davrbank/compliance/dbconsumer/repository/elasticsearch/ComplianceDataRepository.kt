package uz.davrbank.compliance.dbconsumer.repository.elasticsearch

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository
import org.springframework.stereotype.Repository
import uz.davrbank.compliance.dbconsumer.domain.elasticsearch.NewComplianceData

@Repository
interface ComplianceDataRepository : ElasticsearchRepository<NewComplianceData, String>
