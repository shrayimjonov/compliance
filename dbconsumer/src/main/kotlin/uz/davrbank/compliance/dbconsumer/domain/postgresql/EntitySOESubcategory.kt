package uz.davrbank.compliance.dbconsumer.domain.postgresql

import jakarta.persistence.*
import java.util.Date

@Entity
class EntitySOESubcategory(
    @ManyToOne(fetch = FetchType.LAZY) var entitySOE: EntitySOE,
    @Id var id: String,
    var label: String?,
    var lastUpdated: Date?,
    @ManyToOne(fetch = FetchType.LAZY) var compliance: Compliance
)
