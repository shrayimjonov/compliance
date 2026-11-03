package uz.davrbank.compliance.dto.request.filter

import uz.davrbank.compliance.enums.ComplianceType
import uz.davrbank.compliance.enums.DataType
import uz.davrbank.compliance.enums.Type

data class LocalComplianceListFilter(
    val type: Type? = null,
    val complianceType: ComplianceType? = null,
    var dataType: DataType? = null,
    val tin: String? = null,
    val pinfl: String? = null,
)
