package uz.davrbank.compliance.dbconsumer.domain.postgresql

import jakarta.persistence.*
import java.util.Date

@Entity
class EntityEnforcement(
    @Id var id: String,
    var enforcementDesc: String?,
    var sourceName: String?,
    var sourceNameAbbrev: String?,
    var administrativeUnitName: String?,
    var isoStandard: String?,
    var lastUpdated: Date?,
    @ManyToOne(fetch = FetchType.LAZY) var compliance: Compliance
)
