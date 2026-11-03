package uz.davrbank.compliance.controller.settings

import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uz.davrbank.compliance.constants.ApiConstants
import uz.davrbank.compliance.dto.request.settings.ElasticSearchRankRangeRequest
import uz.davrbank.compliance.dto.response.ResponseDTO
import uz.davrbank.compliance.dto.response.settings.ElasticSearchRankRangeResponse
import uz.davrbank.compliance.service.SettingsService

@Validated
@RestController
@RequestMapping(ApiConstants.SETTINGS_ROOT_API)
class SettingsController(
    private val settingsService: SettingsService,
) {

    @GetMapping(ApiConstants.ELASTICSEARCH_RANK_RANGE)
    fun getElasticSearchRankRange(): ResponseEntity<ResponseDTO<ElasticSearchRankRangeResponse>> =
        ResponseEntity.ok(
            ResponseDTO.ok(settingsService.getElasticSearchRankRange())
        )

    @PutMapping(ApiConstants.ELASTICSEARCH_RANK_RANGE)
    fun updateElasticSearchRankRange(
        @Valid @RequestBody request: ElasticSearchRankRangeRequest,
    ): ResponseEntity<ResponseDTO<ElasticSearchRankRangeResponse>> = ResponseEntity.ok(
        ResponseDTO.ok(settingsService.updateElasticSearchRankRange(request))
    )
}
