package uz.davrbank.compliance.dbconsumer.util

import mu.KotlinLogging
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date

object DateConversionUtils {
    private val logger = KotlinLogging.logger {}

    private val STANDARD_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")

    private val UTC_ZONE: ZoneId = ZoneId.of("UTC")

    fun parseTimestamp(timestamp: String?): Date {
        if (timestamp.isNullOrBlank()) return Date()
        return try {
            val dateTime = LocalDateTime.parse(timestamp, STANDARD_FORMATTER)
            Date.from(dateTime.atZone(UTC_ZONE).toInstant())
        } catch (e: Exception) {
            logger.warn { "Failed to parse timestamp: $timestamp - ${e.message}" }
            Date()
        }
    }

    fun createLocalDate(year: Int?, month: Int?, day: Int?): LocalDate? {
        if (year == null) return null
        return try {
            val validMonth = month?.coerceIn(1, 12) ?: 1
            val validDay = day?.coerceIn(1, 31) ?: 1
            LocalDate.of(year, validMonth, validDay)
        } catch (e: Exception) {
            logger.warn { "Failed to create LocalDate from Y:$year M:$month D:$day - ${e.message}" }
            null
        }
    }
}
