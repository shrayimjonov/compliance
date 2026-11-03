package uz.davrbank.compliance.dto.response.local

import uz.davrbank.compliance.domain.Compliance
import uz.davrbank.compliance.domain.EntityAddress
import uz.davrbank.compliance.domain.EntityPosition
import uz.davrbank.compliance.domain.EntityRemark
import uz.davrbank.compliance.domain.EntitySourceItem
import uz.davrbank.compliance.enums.ComplianceType
import uz.davrbank.compliance.enums.DataType
import uz.davrbank.compliance.enums.EntityType
import java.time.LocalDate
import java.util.Date

data class AddressAdminResponse(
    val id: String,
    val addressTypeDesc: String?,
    val city: String?,
    val stateProvinceRegion: String?,
    val country: String?,
    val isoStandard: String?,
    val lastUpdated: Date?,
    val postalCode: String?,
    val address1: String?,
    val address2: String?,
    val entityType: EntityType = EntityType.ENTITY_ADDRESS,
)

data class PositionAdminResponse(
    val id: String,
    val position: String?,
    val lastUpdated: Date?,
    val entityType: EntityType = EntityType.ENTITY_POSITION,
)

data class RemarkAdminResponse(
    val id: String,
    val remark: String?,
    val lastUpdated: Date?,
    val entityType: EntityType = EntityType.ENTITY_REMARK,
)

data class SourceItemAdminResponse(
    val id: String,
    val sourceURI: String?,
    val lastUpdated: Date?,
    val entityType: EntityType = EntityType.ENTITY_SOURCE_ITEM,
)

data class ComplianceAdminResponse(
    val id: String,
    val type: String,
    val firstname: String?,
    val middleName: String?,
    val lastname: String?,
    val gender: String?,
    val title: String?,
    val isDeceased: Boolean?,
    val deceasedDate: LocalDate?,
    val complianceType: ComplianceType,
    val sourceItems: List<SourceItemAdminResponse>,
    val dataType: DataType?,
    val addresses: List<AddressAdminResponse>,
    val remarks: List<RemarkAdminResponse>,
    val positions: List<PositionAdminResponse>,
    val tin: String? = null,
    val pinfl: String? = null,
    val entityType: EntityType = EntityType.COMPLIANCE,
)

internal fun EntityAddress.toAdminResponse() = AddressAdminResponse(
    id = id,
    addressTypeDesc = addressTypeDesc,
    city = city,
    stateProvinceRegion = stateProvinceRegion,
    country = country,
    isoStandard = isoStandard,
    lastUpdated = lastUpdated,
    postalCode = postalCode,
    address1 = address1,
    address2 = address2,
)

internal fun EntityPosition.toAdminResponse() = PositionAdminResponse(
    id = id,
    position = position,
    lastUpdated = lastUpdated,
)

internal fun EntityRemark.toAdminResponse() = RemarkAdminResponse(
    id = id,
    remark = remark,
    lastUpdated = lastUpdated,
)

internal fun EntitySourceItem.toAdminResponse() = SourceItemAdminResponse(
    id = id,
    sourceURI = sourceURI,
    lastUpdated = lastUpdated,
)

internal fun Compliance.toAdminResponse(
    sourceItems: List<SourceItemAdminResponse>,
    addresses: List<AddressAdminResponse>,
    remarks: List<RemarkAdminResponse>,
    positions: List<PositionAdminResponse>,
) = ComplianceAdminResponse(
    id = id,
    type = type.name,
    firstname = firstName,
    middleName = middleName,
    lastname = lastName,
    gender = gender,
    title = title,
    isDeceased = isDeceased,
    deceasedDate = deceasedDate,
    complianceType = complianceType,
    sourceItems = sourceItems,
    dataType = dataType,
    addresses = addresses,
    remarks = remarks,
    positions = positions,
    tin = tin,
    pinfl = pinfl,
)
