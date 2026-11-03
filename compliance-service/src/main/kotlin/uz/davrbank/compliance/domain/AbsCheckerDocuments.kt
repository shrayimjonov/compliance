package uz.davrbank.compliance.domain

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import uz.davrbank.compliance.enums.AbsCheckOperationType
import uz.davrbank.compliance.enums.AbsComplianceStatus
import uz.davrbank.compliance.enums.AbsHandlerStatus
import uz.davrbank.compliance.enums.AbsRequestType
import java.math.BigDecimal
import java.time.LocalDate
import java.util.Date

@Document(collection = "abs_data_logs")
class AbsCheckerRecord(
    var request: AbsCheckerRequestPayload,
    var response: AbsCheckerStoredSearchResponse? = null,
    var status: AbsHandlerStatus = AbsHandlerStatus.CREATED,
    var complianceStatus: AbsComplianceStatus = AbsComplianceStatus.NOT_CHECKED,
    var operationType: AbsCheckOperationType?,
    var comment: String? = null,
    @Indexed(expireAfter = "30d")
    val createdDate: Date = Date(),
    @Id
    var id: String? = null,
)

data class AbsCheckerRequestPayload(
    val isResident: Boolean? = false,
    val firstname: String? = null,
    val lastname: String? = null,
    val patronymic: String? = null,
    val gender: String? = null,
    val birthday: LocalDate? = null,
    val nationality: String? = null,
    val type: AbsRequestType? = AbsRequestType.Organization,
    val clientInn: String? = null,
    val orgName: String? = null,
    val country: String? = null,
    val operationType: AbsCheckOperationType? = AbsCheckOperationType.OPEN_ACCOUNT,
    val swiftCode: String? = null,
    val bankBik: String? = null,
    val address: String? = null,
    val director: String? = null,
    val directorInn: String? = null,
    val founder: String? = null,
    val founderInn: String? = null,
    val partnerName: String? = null,
    val partnerInn: String? = null,
    val orgRegistrationDate: Date? = null,
    val participationShare: BigDecimal? = null,
    val branch: String? = null,
    val actionType: String? = null,
    val requestedDate: Date? = null,
    val comment: String? = null,
)

data class AbsCheckerStoredSearchResponse(
    val took: Long,
    val hits: AbsCheckerStoredHits,
)

data class AbsCheckerStoredHits(
    val total: Long,
    val maxScore: Double,
    val hits: List<AbsCheckerStoredHit>,
)

data class AbsCheckerStoredHit(
    val id: String,
    val score: Double,
    val name: String? = null,
    val fullName: String? = null,
    val firstname: String? = null,
    val lastname: String? = null,
    val identificationNumber: String? = null,
    val region: String? = null,
    val birth: String? = null,
    val entityAdverseMedia: Boolean = false,
    val extLookUpId: String? = null,
    val type: String? = null,
    val complianceType: String? = null,
    val dataType: String? = null,
)
