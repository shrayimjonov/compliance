package uz.davrbank.compliance.dbconsumer.domain.postgresql

import jakarta.persistence.*
import uz.davrbank.compliance.dbconsumer.enums.ComplianceType
import java.util.Date

@Entity
class EntitySanction(
    @ManyToOne(fetch = FetchType.LAZY) val compliance: Compliance,
    @Id var id: String,
    var subCategoryLabel: String?,
    var sourceName: String?,
    var sourceNameAbbrev: String?,
    var administrativeUnitName: String?,
    var isoStandard: String?,
    var lastUpdated: Date,
    @Enumerated(EnumType.STRING) var complianceType: ComplianceType,
    @Embedded var consolidatedSanction: ConsolidatedSanction
)
