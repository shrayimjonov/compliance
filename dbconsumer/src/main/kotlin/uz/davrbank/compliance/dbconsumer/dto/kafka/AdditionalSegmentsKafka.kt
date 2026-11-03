package uz.davrbank.compliance.dbconsumer.dto.kafka

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty

data class AdditionalSegmentsKafka(
    @JsonProperty("Segment")
    @JsonFormat(with = [JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY])
    val segment: List<SegmentKafka>
)

data class SegmentKafka(
    @JacksonXmlProperty(isAttribute = true, localName = "Type")
    val type: String?,
    @JsonProperty("Record")
    @JsonFormat(with = [JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY])
    val record: List<RecordKafka>?,
)

data class RecordKafka(
    @JacksonXmlProperty(isAttribute = true, localName = "GUID")
    val guid: String?,
    @JsonProperty("LastUpdated")
    val lastUpdated: String,
    @JsonProperty("Field")
    @JsonFormat(with = [JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY])
    val field: List<FieldKafka>?,
)

data class FieldKafka(
    @JsonProperty("DerivedName")
    val derivedName: String,
    @JsonProperty("DerivedValue")
    val derivedValue: String
)