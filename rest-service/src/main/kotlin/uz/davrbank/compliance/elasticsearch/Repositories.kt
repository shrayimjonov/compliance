package uz.davrbank.compliance.elasticsearch


import org.springframework.data.elasticsearch.repository.ElasticsearchRepository

//interface ComplianceEntityRepository : ElasticsearchRepository<ComplianceEntityDoc, String> {
//
//}


interface ComplianceDataRepository: ElasticsearchRepository<NewComplianceData, String> {

}