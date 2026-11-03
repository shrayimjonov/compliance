package uz.davrbank.compliance.dbconsumer.domain.postgresql

import jakarta.persistence.*
import java.util.Date

@Entity
class EntitySourceItem(
    @Id var id: String,
    @Column(columnDefinition = "TEXT") var sourceURI: String?,
    var lastUpdated: Date?,
    @ManyToOne(fetch = FetchType.LAZY) var compliance: Compliance
)
