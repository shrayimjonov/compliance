package uz.davrbank.compliance.cache

import org.springframework.data.mongodb.repository.MongoRepository

interface ComplianceCacheRepository : MongoRepository<ComplianceCache, String> {
    fun findByHash(hash: String): ComplianceCache?
}