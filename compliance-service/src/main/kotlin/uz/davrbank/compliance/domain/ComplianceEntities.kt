package uz.davrbank.compliance.domain

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import jakarta.persistence.Embedded
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import uz.davrbank.compliance.domain.base.BaseEntity
import uz.davrbank.compliance.enums.ComplianceType
import uz.davrbank.compliance.enums.DataType
import uz.davrbank.compliance.enums.Type
import java.time.LocalDate
import java.util.Date

@Entity
@Table(
    indexes = [
        Index(name = "idx_pinfl", columnList = "pinfl"),
        Index(name = "idx_compliance_type", columnList = "compliance_type"),
    ]
)
class Compliance(
    @Id
    var id: String,
    @Enumerated(EnumType.STRING)
    var type: Type,
    var gender: String,
    var name: String,
    var firstName: String?,
    var middleName: String?,
    var lastName: String,
    var fullName: String,
    var prefix: String?,
    var suffix: String?,
    @Column(columnDefinition = "TEXT")
    var title: String?,
    var isDeceased: Boolean?,
    var deceasedDate: LocalDate?,
    var entityId: Long?,
    var lookupId: String?,
    var lastUpdated: Date,
    @Enumerated(EnumType.STRING)
    var complianceType: ComplianceType,
    @Enumerated(EnumType.STRING)
    var dataType: DataType,
    var tin: String? = null,
    var pinfl: String? = null,
    var createdDate: Date = Date(),
)

@Entity
class EntityDOB(
    @Id
    var id: String,
    var birthDate: LocalDate?,
    var lastUpdated: Date,
    @ManyToOne(fetch = FetchType.LAZY)
    var compliance: Compliance,
)

@Entity
class EntityAddress(
    @Id
    var id: String,
    var addressTypeDesc: String?,
    var city: String?,
    var stateProvinceRegion: String?,
    var country: String?,
    var isoStandard: String?,
    var lastUpdated: Date?,
    var postalCode: String?,
    var address1: String?,
    var address2: String?,
    @ManyToOne(fetch = FetchType.LAZY)
    var compliance: Compliance,
)

@Entity
class EntityAlias(
    @Id
    var id: String,
    val aliasTypeDesc: String?,
    val englishDescription: String?,
    val firstName: String?,
    val lastName: String?,
    val lastUpdated: Date?,
    val middleName: String?,
    val name: String?,
    val prefix: String?,
    val suffix: String?,
    @ManyToOne(fetch = FetchType.LAZY)
    var compliance: Compliance,
)

@Entity
class EntityIdentification(
    @Id
    var id: String,
    val administrativeUnitName: String?,
    val expiration: LocalDate?,
    val isoStandard: String?,
    val identificationIssuer: String?,
    val identificationNumber: String?,
    val identificationTypeDesc: String?,
    val issueDate: LocalDate?,
    val lastUpdated: Date?,
    @ManyToOne(fetch = FetchType.LAZY)
    var compliance: Compliance,
)

@Entity
class EntityCountryAssociation(
    @ManyToOne(fetch = FetchType.LAZY)
    var compliance: Compliance,
    @Id
    var id: String,
    var administrativeUnitName: String?,
    var associationTypeDesc: String?,
    var isostandard: String?,
    var lastUpdated: Date?,
    var ownershipPercentageCalc: String? = null,
    @Enumerated(EnumType.STRING)
    var complianceType: ComplianceType,
)

@Entity
class EntityPosition(
    @Id
    var id: String,
    @Column(columnDefinition = "TEXT")
    var position: String?,
    var lastUpdated: Date?,
    @ManyToOne(fetch = FetchType.LAZY)
    var compliance: Compliance,
)

@Entity
@Table(
    indexes = [
        Index(name = "idx_relatedEntityGuid", columnList = "relatedEntityGUID"),
    ]
)
class EntityRelationship(
    @Id
    var id: String,
    var relatedEntityGUID: String?,
    var groupDesc: String?,
    var relationshipDesc: String?,
    var lastUpdated: Date?,
    @ManyToOne(fetch = FetchType.LAZY)
    var compliance: Compliance,
    var ownershipPercentage: String? = null,
    @Enumerated(EnumType.STRING)
    var complianceType: ComplianceType,
)

@Entity
class EntityRemark(
    @Id
    var id: String,
    @Column(columnDefinition = "TEXT")
    var remark: String?,
    var lastUpdated: Date?,
    @ManyToOne(fetch = FetchType.LAZY)
    var compliance: Compliance,
)

@Entity
class EntitySourceItem(
    @Id
    var id: String,
    @Column(columnDefinition = "TEXT")
    var sourceURI: String?,
    var lastUpdated: Date?,
    @ManyToOne(fetch = FetchType.LAZY)
    var compliance: Compliance,
)

@Entity
class EntitySegment(
    var entityPep: Boolean = false,
    var entityAdverseMedia: Boolean = false,
    var entityEnforcement: Boolean = false,
    var entitySanction: Boolean = false,
    var entitySOE: Boolean = false,
    var additionalSegment: Boolean = false,
    @ManyToOne(fetch = FetchType.LAZY)
    var compliance: Compliance,
) : BaseEntity()

@Entity
class EntityPEP(
    @Id
    var id: String,
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
    @ManyToOne(fetch = FetchType.LAZY)
    var compliance: Compliance,
)

@Entity
class EntityPEPSubcategory(
    @ManyToOne(fetch = FetchType.LAZY)
    var entityPep: EntityPEP,
    @Id
    var id: String,
    var pepSubcategoryDesc: String?,
    var subCategoryLabel: String?,
    var lastUpdated: Date?,
    @ManyToOne(fetch = FetchType.LAZY)
    var compliance: Compliance,
)

@Entity
class EntityAdverseMedia(
    @Id
    var id: String,
    var adverseMediaDesc: String?,
    var lastUpdated: Date?,
    @ManyToOne(fetch = FetchType.LAZY)
    var compliance: Compliance,
)

@Entity
class EntityAdverseMediaSubcategory(
    @ManyToOne(fetch = FetchType.LAZY)
    var entityAdverseMedia: EntityAdverseMedia,
    @Id
    var id: String,
    var subCategoryLabel: String?,
    var lastUpdated: Date?,
    @ManyToOne(fetch = FetchType.LAZY)
    var compliance: Compliance,
)

@Entity
class EntityEnforcement(
    @Id
    var id: String,
    var enforcementDesc: String?,
    var sourceName: String?,
    var sourceNameAbbrev: String?,
    var administrativeUnitName: String?,
    var isoStandard: String?,
    var lastUpdated: Date?,
    @ManyToOne(fetch = FetchType.LAZY)
    var compliance: Compliance,
)

@Entity
class EntityEnforcementSubcategory(
    @ManyToOne(fetch = FetchType.LAZY)
    var entityEnforcement: EntityEnforcement,
    @Id
    var id: String,
    var subCategoryLabel: String,
    var lastUpdated: Date,
    @ManyToOne(fetch = FetchType.LAZY)
    var compliance: Compliance,
)

@Entity
class EntitySanction(
    @ManyToOne(fetch = FetchType.LAZY)
    val compliance: Compliance,
    @Id
    var id: String,
    var subCategoryLabel: String?,
    var sourceName: String?,
    var sourceNameAbbrev: String?,
    var administrativeUnitName: String?,
    var isoStandard: String?,
    var lastUpdated: Date,
    @Enumerated(EnumType.STRING)
    var complianceType: ComplianceType,
    @Embedded
    var consolidatedSanction: ConsolidatedSanction,
)

@Embeddable
data class ConsolidatedSanction(
    var consolidatedSanctionGUID: String?,
    var entityGUID: String?,
    var lastUpdated: Date?,
)

@Entity
class EntitySOE(
    @ManyToOne(fetch = FetchType.LAZY)
    var compliance: Compliance,
    @Id
    var id: String,
    var isActive: Boolean,
    var lastUpdated: Date,
)

@Entity
class EntitySOESubcategory(
    @ManyToOne(fetch = FetchType.LAZY)
    var entitySOE: EntitySOE,
    @Id
    var id: String,
    var label: String?,
    var lastUpdated: Date?,
    @ManyToOne(fetch = FetchType.LAZY)
    var compliance: Compliance,
)

@Entity
class EntitySOEDomain(
    @ManyToOne(fetch = FetchType.LAZY)
    var entitySOE: EntitySOE,
    @Id
    var id: String,
    val soeDomainDesc: String,
    val lastUpdated: Date,
    @ManyToOne(fetch = FetchType.LAZY)
    val compliance: Compliance,
)

@Entity
class AdditionalSegment(
    @ManyToOne(fetch = FetchType.LAZY)
    var compliance: Compliance,
    var type: String?,
) : BaseEntity()

@Entity
class Record(
    @ManyToOne(fetch = FetchType.LAZY)
    val additionalSegment: AdditionalSegment,
    val lastUpdated: Date?,
    @Id
    var id: String,
    @ManyToOne(fetch = FetchType.LAZY)
    val compliance: Compliance,
)

@Entity
class Field(
    @ManyToOne(fetch = FetchType.LAZY)
    val record: Record,
    val derivedName: String?,
    val derivedValue: String?,
    @ManyToOne(fetch = FetchType.LAZY)
    val compliance: Compliance,
) : BaseEntity()
