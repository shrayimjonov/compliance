package uz.davrbank.compliance.dbconsumer.domain.postgresql

import jakarta.persistence.*
import uz.davrbank.compliance.dbconsumer.enums.ComplianceType
import java.util.Date

@Entity
@Table(
    indexes = [
        Index(name = "idx_relatedEntityGuid", columnList = "relatedEntityGUID")
    ]
)
class EntityRelationship(
    @Id var id: String,
    var relatedEntityGUID: String?,
    var groupDesc: String?,
    var relationshipDesc: String?,
    var lastUpdated: Date?,
    @ManyToOne var compliance: Compliance,
    var ownershipPercentage: String? = null,
    @Enumerated(EnumType.STRING) var complianceType: ComplianceType
)
