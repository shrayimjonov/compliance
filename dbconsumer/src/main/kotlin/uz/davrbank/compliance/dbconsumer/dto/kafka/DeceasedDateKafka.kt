package uz.davrbank.compliance.dbconsumer.dto.kafka

import com.fasterxml.jackson.annotation.JsonProperty

data class DeceasedDateKafka(
    @JsonProperty("DeceasedDay")
    val deceasedDay: Int?,
    @JsonProperty("DeceasedMonth")
    val deceasedMonth: Int?,
    @JsonProperty("DeceasedYear")
    val deceasedYear: Int?,
)
