package uz.davrbank.compliance.service.impl

import org.springframework.stereotype.Service
import uz.davrbank.compliance.dto.request.settings.ElasticSearchRankRangeRequest
import uz.davrbank.compliance.dto.response.settings.ElasticSearchRankRangeResponse
import uz.davrbank.compliance.exception.BadRequestException
import uz.davrbank.compliance.service.SettingsService
import java.util.concurrent.atomic.AtomicReference

@Service
class SettingsServiceImpl : SettingsService {

    private val elasticSearchRankRange = AtomicReference(
        ElasticSearchRankRangeResponse(
            minRange = DEFAULT_MIN_RANGE,
            maxRange = DEFAULT_MAX_RANGE,
        )
    )

    override fun getElasticSearchRankRange(): ElasticSearchRankRangeResponse =
        elasticSearchRankRange.get()

    override fun updateElasticSearchRankRange(
        request: ElasticSearchRankRangeRequest,
    ): ElasticSearchRankRangeResponse {
        val minRange = request.minRange
            ?: throw BadRequestException("Property 'minRange' must be provided.")
        val maxRange = request.maxRange
            ?: throw BadRequestException("Property 'maxRange' must be provided.")

        if (minRange < MIN_ALLOWED_RANGE || maxRange > MAX_ALLOWED_RANGE) {
            throw BadRequestException("Elasticsearch rank range must be between 0 and 100.")
        }

        if (minRange > maxRange) {
            throw BadRequestException("Property 'minRange' must be less than or equal to 'maxRange'.")
        }

        val updatedRange = ElasticSearchRankRangeResponse(
            minRange = minRange,
            maxRange = maxRange,
        )
        elasticSearchRankRange.set(updatedRange)

        return updatedRange
    }

    companion object {
        const val DEFAULT_MAX_RANGE = 100.0
        const val DEFAULT_MIN_RANGE = 50.0
        private const val MAX_ALLOWED_RANGE = 100.0
        private const val MIN_ALLOWED_RANGE = 0.0
    }
}
