package uz.davrbank.compliance.elasticsearch

import org.springframework.data.annotation.Id
import org.springframework.data.elasticsearch.annotations.Document
import uz.davrbank.compliance.ComplianceType
import uz.davrbank.compliance.DataType

//@Document(indexName = "my_compliance_entity")
//data class ComplianceEntityDoc(
//    var firstname: String,
//    var lastname: String,
//    var fullName: String, // "$firstname $lastname"
//    var extLookUpId: String,
//    var type: String, // compliance entity type: Organization, Individual,
//    var complianceType: ComplianceType,
//    @Id var id: String,
//    var dataType: DataType,
//)


@Document(indexName = "compliance_data_v3")
data class NewComplianceData(
    var firstname: String,
    var lastname: String,
    var fullName: String, // "$firstname $lastname"
    var extLookUpId: String,
    var type: String, // compliance entity type: Organization, Individual,
    var complianceType: ComplianceType,
    @Id var id: String,
    var dataType: DataType,
    var birth: String? = null, // tug'ilgan kun berilmagan bo'lsa
    var region: String = "",
)
