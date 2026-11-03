package uz.davrbank.compliance.dbconsumer.domain.postgresql

import jakarta.persistence.*
import java.time.LocalDate
import java.util.Date

@Entity
class EntityPEP(
    @Id var id: String,
    var isPrimaryPEP: Long?,
    var isActivePEP: String?,
    var isInCountryPEPOnly: String?,
    var pepAdminLevelDesc: String?,
    var isoAdministrativeUnitLevel0: String?,
    var administrativeUnitLevel0: String?,
    var governingInstitution: String?,
    var governingRole: String?,
    var lastUpdated: Date?,
    var effectiveDate: LocalDate?,
    var expirationDate: LocalDate?,
    @ManyToOne(fetch = FetchType.LAZY) var compliance: Compliance
)
