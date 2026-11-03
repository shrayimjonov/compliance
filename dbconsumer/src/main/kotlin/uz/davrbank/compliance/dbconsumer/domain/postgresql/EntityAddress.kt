package uz.davrbank.compliance.dbconsumer.domain.postgresql

import jakarta.persistence.*
import java.util.Date

@Entity
class EntityAddress(
    @Id var id: String,
    var addressTypeDesc: String?,
    var city: String?,
    var stateProvinceRegion: String?,
    var country: String?,
    var isoStandard: String?,
    var lastUpdated: Date?,
    var postalCode: String?,
    var address1: String?,
    var address2: String?,
    @ManyToOne var compliance: Compliance
)
