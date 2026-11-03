package uz.davrbank.compliance.dbconsumer.dto.internal

import uz.davrbank.compliance.dbconsumer.domain.postgresql.AdditionalSegment
import uz.davrbank.compliance.dbconsumer.domain.postgresql.Field
import uz.davrbank.compliance.dbconsumer.domain.postgresql.Record

data class AdditionalSegmentInternalDto(
    var additionalSegment: AdditionalSegment,
    var recordInternal: List<RecordInternalDto>
)

data class RecordInternalDto(
    val record: Record,
    val field: List<Field>,
)
