package uz.davrbank.compliance.dbconsumer.domain.postgresql

import jakarta.persistence.*
import uz.davrbank.compliance.dbconsumer.enums.ComplianceType
import uz.davrbank.compliance.dbconsumer.enums.DataType
import uz.davrbank.compliance.dbconsumer.enums.Type
import java.time.LocalDate
import java.util.Date

@Entity
@Table(
    indexes = [
        Index(name = "idx_pinfl", columnList = "pinfl"),
        Index(name = "idx_compliance_type", columnList = "complianceType")
    ]
)
class Compliance(
    @Id var id: String,
    @Enumerated(EnumType.STRING) var type: Type,
    var gender: String,
    var name: String,
    var firstName: String?,
    var middleName: String?,
    var lastName: String,
    var fullName: String,
    var prefix: String?,
    var suffix: String?,
    @Column(columnDefinition = "TEXT") var title: String?,
    var isDeceased: Boolean?,
    var deceasedDate: LocalDate?,
    var entityId: Long?,
    var lookupId: String?,
    var lastUpdated: Date,
    @Enumerated(EnumType.STRING) var complianceType: ComplianceType,
    @Enumerated(EnumType.STRING) var dataType: DataType,
    var tin: String? = null,
    var pinfl: String? = null,
    var createdDate: Date = Date(),
    @OneToMany(mappedBy = "compliance", fetch = FetchType.LAZY)
    val identifications: Set<EntityIdentification> = emptySet(),

    @OneToMany(mappedBy = "compliance", fetch = FetchType.LAZY)
    val countryAssociations: Set<EntityCountryAssociation> = emptySet(),

    @OneToMany(mappedBy = "compliance", fetch = FetchType.LAZY)
    val entityDOBs: Set<EntityDOB> = emptySet(),
) {
    constructor() : this(
        id = "",
        type = Type.Individual,
        gender = "",
        name = "",
        firstName = null,
        middleName = null,
        lastName = "",
        fullName = "",
        prefix = null,
        suffix = null,
        title = null,
        isDeceased = null,
        deceasedDate = null,
        entityId = null,
        lookupId = null,
        lastUpdated = Date(),
        complianceType = ComplianceType.ENTITY,
        dataType = DataType.LEXIS,
        identifications = emptySet(),
        countryAssociations = emptySet(),
    )
}