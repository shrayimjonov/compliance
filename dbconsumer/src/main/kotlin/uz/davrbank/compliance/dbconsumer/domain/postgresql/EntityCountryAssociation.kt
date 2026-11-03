package uz.davrbank.compliance.dbconsumer.domain.postgresql

import jakarta.persistence.*
import uz.davrbank.compliance.dbconsumer.enums.ComplianceType
import java.util.Date

@Entity
class EntityCountryAssociation(
    @ManyToOne(fetch = FetchType.LAZY) var compliance: Compliance,
    @Id var id: String,
    var administrativeUnitName: String?,
    var associationTypeDesc: String?,
    var isostandard: String?,
    var lastUpdated: Date?,
    var ownershipPercentageCalc: String? = null,
    @Enumerated(EnumType.STRING) var complianceType: ComplianceType
)
