package uz.davrbank.compliance.dbconsumer.domain.postgresql

import jakarta.persistence.*
import java.util.Date

@Entity
class Record(
    @ManyToOne(fetch = FetchType.LAZY) val additionalSegment: AdditionalSegment,
    val lastUpdated: Date?,
    @Id var id: String,
    @ManyToOne(fetch = FetchType.LAZY) val compliance: Compliance
)
