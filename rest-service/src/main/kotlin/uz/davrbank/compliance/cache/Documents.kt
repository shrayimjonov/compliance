package uz.davrbank.compliance.cache

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import uz.davrbank.compliance.local.ComplianceSearchRequest
import uz.davrbank.compliance.elasticsearch.ESHits

@Document
class ComplianceCache(
    @Indexed(unique = true, expireAfterSeconds = 1 * 24 * 60 * 60) val hash: String, // expires after 1 days
    val request: ComplianceSearchRequest,
    val hits: ESHits,
    @Id val id: String? = null
)