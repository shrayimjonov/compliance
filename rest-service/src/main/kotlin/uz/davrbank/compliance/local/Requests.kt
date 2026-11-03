package uz.davrbank.compliance.local

import uz.davrbank.compliance.ComplianceType
import uz.davrbank.compliance.DataType
import uz.davrbank.compliance.backup.Compliance
import uz.davrbank.compliance.backup.EntityAddress
import uz.davrbank.compliance.backup.Type
import uz.davrbank.compliance.elasticsearch.SearchTarget
import java.time.LocalDate
import java.util.*

data class ComplianceSearchRequest(
    val firstname: String? = null,
    val lastname: String? = null,
    val name: String? = null,
    val target: SearchTarget
)

data class RemarkRequest(
    var id: String? = null,
    var remark: String?
)

data class SourceItemRequest(
    var id: String ? = null,
    var sourceURI: String?
)

data class PositionRequest(
    var id: String? = null,
    var position: String?,
)

data class AddressRequest(
    var id: String? = null,
    var addressTypeDesc: String?,
    var city: String?,
    var stateProvinceRegion: String?,
    var country: String?,
    var isoStandard: String?,
    var postalCode: String?,
    var address1: String?,
    var address2: String?
) {
    companion object {
        fun toEntity(request: AddressRequest, compliance: Compliance): EntityAddress {
            return EntityAddress(
                UUID.randomUUID().toString(),
                request.addressTypeDesc,
                request.city,
                request.stateProvinceRegion,
                request.country,
                request.isoStandard,
                Date(),
                request.postalCode,
                request.address1,
                request.address2,
                compliance
            )
        }
    }
}

data class ComplianceAdminRequest(
    val type: Type,
    val firstname: String,
    val middleName: String?,
    val lastname: String?,
    val gender: String?,
    val title: String?,
    var isDeceased: Boolean?,
    var deceasedDate: LocalDate?,
    var complianceType: ComplianceType,
    var sourceItemRequest: List<SourceItemRequest>,
    var dataType: DataType,
    var addressRequest: List<AddressRequest>,
    var remark: List<RemarkRequest>,
    var position: List<PositionRequest>,
    var tin: String? = null,
    var pinfl: String? = null
) {
    companion object {
        fun ComplianceAdminRequest.toEntity(): Compliance {
            return Compliance(
                id = UUID.randomUUID().toString(),
                type = this.type,
                gender = this.gender ?: "Not applicable",
                name = "${this.firstname} ${this.lastname ?: ""}".trim(),
                firstName = this.firstname,
                middleName = this.middleName,
                lastName = this.lastname ?: "",
                fullName = "${this.firstname} ${this.lastname ?: ""}".trim(),
                prefix = null,
                suffix = null,
                title = this.title,
                isDeceased = this.isDeceased,
                deceasedDate = this.deceasedDate,
                entityId = null, // Set this based on your logic
                lookupId = null, // Set this based on your logic
                lastUpdated = Date(),
                complianceType = this.complianceType,
                dataType = this.dataType,
                tin = this.tin,
                pinfl = this.pinfl
            )
        }
    }
}