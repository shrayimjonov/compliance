package uz.davrbank.compliance.service

import uz.davrbank.compliance.dto.request.filter.BaseFilter
import uz.davrbank.compliance.dto.request.filter.OrgSearch
import uz.davrbank.compliance.dto.response.ElasticsearchSearchResponse

interface ElasticsearchSearchService {
    fun search(filter: BaseFilter<OrgSearch>): ElasticsearchSearchResponse
}
