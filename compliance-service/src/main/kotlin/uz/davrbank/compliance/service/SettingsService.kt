package uz.davrbank.compliance.service

import uz.davrbank.compliance.dto.request.settings.ElasticSearchRankRangeRequest
import uz.davrbank.compliance.dto.response.settings.ElasticSearchRankRangeResponse

interface SettingsService {
    fun getElasticSearchRankRange(): ElasticSearchRankRangeResponse
    fun updateElasticSearchRankRange(request: ElasticSearchRankRangeRequest): ElasticSearchRankRangeResponse
}
