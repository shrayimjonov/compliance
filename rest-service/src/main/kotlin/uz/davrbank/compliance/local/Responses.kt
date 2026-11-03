package uz.davrbank.compliance.local

import uz.davrbank.compliance.ComplianceType
import uz.davrbank.compliance.DataType
import uz.davrbank.compliance.backup.*
import java.time.LocalDate
import java.util.*

data class AddressAdminResponse(
    val id: String,
    var addressTypeDesc: String?,
    var city: String?,
    var stateProvinceRegion: String?,
    var country: String?,
    var isoStandard: String?,
    var lastUpdated: Date?,
    var postalCode: String?,
    var address1: String?,
    var address2: String?,
) {
    companion object {
        fun toResponse(entityAddress: EntityAddress) = entityAddress.run {
            AddressAdminResponse(
                id,
                addressTypeDesc,
                city,
                stateProvinceRegion,
                country,
                isoStandard,
                lastUpdated,
                postalCode,
                address1,
                address2
            )
        }
    }
}


data class PositionAdminResponse(
    var id: String,
    var position: String?,
    var lastUpdated: Date?
) {
    companion object {
        fun toResponse(positionEntity: EntityPosition) = positionEntity.run {
            PositionAdminResponse(
                id,
                position,
                lastUpdated
            )
        }
    }
}


data class RemarkAdminResponse(
    var id: String,
    var remark: String?,
    var lastUpdated: Date?
) {
    companion object {
        fun toResponse(entityRemark: EntityRemark) = entityRemark.run {
            RemarkAdminResponse(
                id,
                remark,
                lastUpdated
            )
        }
    }
}


data class ComplianceAdminResponse(
    val id: String,
    val type: String,
    val firstname: String?,
    val middleName: String?,
    val lastname: String?,
    val gender: String?,
    val title: String?,
    var isDeceased: Boolean?,
    var deceasedDate: LocalDate?,
    var complianceType: ComplianceType,
    var sourceItems: List<SourceItemAdminResponse>,
    var dataType: DataType?,
    var addresses: List<AddressAdminResponse>,
    var remarks: List<RemarkAdminResponse>,
    var positions: List<PositionAdminResponse>,
    var tin: String? = null,
    var pinfl: String? = null
) {
    companion object {
        fun toResponse(
            compliance: Compliance,
            sourceItems: List<SourceItemAdminResponse>,
            addresses: List<AddressAdminResponse>,
            remarks: List<RemarkAdminResponse>, positions: List<PositionAdminResponse>
        ) = compliance.run {
            ComplianceAdminResponse(
                id,
                type.name,
                firstName,
                middleName,
                lastName,
                gender,
                title,
                isDeceased,
                deceasedDate,
                complianceType,
                sourceItems,
                dataType,
                addresses,
                remarks,
                positions,
                tin,
                pinfl
            )
        }
    }
}



data class SourceItemAdminResponse(
    var id: String,
    var sourceURI: String?,
    var lastUpdated: Date?
) {
    companion object {
        fun toResponse(sourceItem: EntitySourceItem) = sourceItem.run {
            SourceItemAdminResponse(
                id,
                sourceURI,
                lastUpdated
            )
        }
    }
}