package uz.davrbank.compliance.dto.request.settings

import jakarta.validation.constraints.DecimalMax
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotNull

data class ElasticSearchRankRangeRequest(
    @field:NotNull
    @field:DecimalMin("0")
    @field:DecimalMax("99")
    var minRange: Double?,
    @field:NotNull
    @field:DecimalMin("1")
    @field:DecimalMax("100")
    var maxRange: Double?,
)
