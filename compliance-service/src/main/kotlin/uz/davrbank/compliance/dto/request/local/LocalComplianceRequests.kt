package uz.davrbank.compliance.dto.request.local

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonSetter
import com.fasterxml.jackson.annotation.Nulls
import jakarta.validation.Valid
import jakarta.validation.constraints.NotNull
import uz.davrbank.compliance.enums.ComplianceType
import uz.davrbank.compliance.enums.DataType
import uz.davrbank.compliance.enums.Type
import java.time.LocalDate

@JsonIgnoreProperties(ignoreUnknown = true)
data class RemarkRequest(
    val id: String? = null,
    val remark: String? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class SourceItemRequest(
    val id: String? = null,
    val sourceURI: String? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class PositionRequest(
    val id: String? = null,
    val position: String? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class AddressRequest(
    val id: String? = null,
    val addressTypeDesc: String? = null,
    val city: String? = null,
    val stateProvinceRegion: String? = null,
    val country: String? = null,
    val isoStandard: String? = null,
    val postalCode: String? = null,
    val address1: String? = null,
    val address2: String? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class ComplianceAdminRequest(
    @field:NotNull
    val type: Type?,
    val firstname: String?,
    val middleName: String? = null,
    val lastname: String? = null,
    val gender: String? = null,
    val title: String? = null,
    val isDeceased: Boolean? = null,
    val deceasedDate: LocalDate? = null,
    var complianceType: ComplianceType?,
    @field:Valid
    @field:JsonAlias("sourceItems")
    @JsonSetter(nulls = Nulls.AS_EMPTY)
    val sourceItemRequest: List<SourceItemRequest> = emptyList(),
    var dataType: DataType?,
    @field:Valid
    @field:JsonAlias("addresses")
    @JsonSetter(nulls = Nulls.AS_EMPTY)
    val addressRequest: List<AddressRequest> = emptyList(),
    @field:Valid
    @field:JsonAlias("remarks")
    @JsonSetter(nulls = Nulls.AS_EMPTY)
    val remark: List<RemarkRequest> = emptyList(),
    @field:Valid
    @field:JsonAlias("positions")
    @JsonSetter(nulls = Nulls.AS_EMPTY)
    val position: List<PositionRequest> = emptyList(),
    val tin: String? = null,
    val pinfl: String? = null,
)
