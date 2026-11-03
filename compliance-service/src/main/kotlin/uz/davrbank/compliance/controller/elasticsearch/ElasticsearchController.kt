package uz.davrbank.compliance.controller.elasticsearch

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uz.davrbank.compliance.annotation.LogApiCall
import uz.davrbank.compliance.constants.ApiConstants
import uz.davrbank.compliance.dto.request.filter.BaseFilter
import uz.davrbank.compliance.dto.request.filter.OrgSearch
import uz.davrbank.compliance.dto.response.ElasticsearchSearchResponse
import uz.davrbank.compliance.dto.response.ResponseDTO
import uz.davrbank.compliance.service.ElasticsearchSearchService
import java.util.UUID

@RestController
@RequestMapping(ApiConstants.ELASTICSEARCH_ROOT_API)
class ElasticsearchController(
    private val elasticsearchSearchService: ElasticsearchSearchService,
) {

    @LogApiCall
    @PostMapping(ApiConstants.SEARCH)
    fun search(
        @RequestBody filter: BaseFilter<OrgSearch>,
    ): ResponseEntity<ResponseDTO<ElasticsearchSearchResponse>> {
        val requestId = filter.requestId
            ?.trim()
            ?.takeIf(String::isNotBlank)
            ?: UUID.randomUUID().toString()
        filter.requestId = requestId

        return ResponseEntity.ok(
            ResponseDTO.ok(elasticsearchSearchService.search(filter), requestId)
        )
    }
}
