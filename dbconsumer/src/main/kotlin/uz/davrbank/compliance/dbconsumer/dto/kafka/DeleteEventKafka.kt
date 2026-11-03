package uz.davrbank.compliance.dbconsumer.dto.kafka

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class DeleteEventKafka(
    @JsonProperty("EntityGUID") val EntityGUID: String,
    @JsonProperty("DateDeleted") val DateDeleted: String
) {
    fun getParsedDate(): LocalDateTime {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")
        return LocalDateTime.parse(DateDeleted, formatter)
    }
}
