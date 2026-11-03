package uz.davrbank.compliance.cache

import org.springframework.stereotype.Service
import uz.davrbank.compliance.local.ComplianceSearchRequest
import uz.davrbank.compliance.local.hashForCache
import uz.davrbank.compliance.elasticsearch.ESHits

interface ComplianceCacheService {
    fun findByRequest(request: ComplianceSearchRequest): ESHits?
    fun cache(request: ComplianceSearchRequest, hits: ESHits)
}

@Service
class ComplianceCacheServiceImpl(
    private val complianceCacheRepository: ComplianceCacheRepository
) : ComplianceCacheService {

    override fun findByRequest(request: ComplianceSearchRequest): ESHits? {
        val cache = complianceCacheRepository.findByHash(request.hashForCache()) ?: return null
        return cache.hits
    }

    override fun cache(request: ComplianceSearchRequest, hits: ESHits) {
        val cache = ComplianceCache(request.hashForCache(), request, hits)
        complianceCacheRepository.save(cache)
    }
}