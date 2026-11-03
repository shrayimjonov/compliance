package uz.davrbank.compliance.util

import kotlin.math.round

internal data class SearchScoreScale(
    val maxRawScore: Double,
) {
    init {
        require(maxRawScore >= 0.0) { "maxRawScore must be greater than or equal to zero." }
    }

    fun normalize(rawScore: Double?): Double {
        val safeRawScore = rawScore
            ?.takeIf(Double::isFinite)
            ?.coerceAtLeast(0.0)
            ?: 0.0

        if (safeRawScore <= 0.0 || maxRawScore <= 0.0) {
            return 0.0
        }

        val normalizedScore = (safeRawScore / maxRawScore) * MAX_RESPONSE_SCORE
        return round(normalizedScore.coerceIn(MIN_RESPONSE_SCORE, MAX_RESPONSE_SCORE) * SCORE_PRECISION) /
            SCORE_PRECISION
    }

    companion object {
        private const val MIN_RESPONSE_SCORE = 1.0
        private const val MAX_RESPONSE_SCORE = 100.0
        private const val SCORE_PRECISION = 100.0
    }
}
