package uz.davrbank.compliance.dbconsumer.domain.elasticsearch

import org.springframework.data.annotation.Id
import org.springframework.data.elasticsearch.annotations.Document
import uz.davrbank.compliance.dbconsumer.enums.ComplianceType
import uz.davrbank.compliance.dbconsumer.enums.DataType

@Document(indexName = "compliance_data_v4")
data class NewComplianceData(
    var firstname: String,
    var lastname: String,
    var fullName: String,
    var extLookUpId: String,
    var type: String,
    var complianceType: ComplianceType,
    @Id var id: String,
    var dataType: DataType,
    var birth: String? = null,
    var region: String = "",
    var entityAdverseMedia: Boolean? = false,
    var name: String,
    var identificationNumber: String?,
)
