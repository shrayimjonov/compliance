package uz.davrbank.compliance.dto.response.abs

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import uz.davrbank.compliance.domain.ApiLog
import uz.davrbank.compliance.domain.AbsCheckerRecord
import uz.davrbank.compliance.domain.AbsCheckerRequestPayload
import uz.davrbank.compliance.domain.AbsCheckerStoredHit
import uz.davrbank.compliance.domain.AbsCheckerStoredSearchResponse
import uz.davrbank.compliance.enums.AbsCheckOperationType
import uz.davrbank.compliance.enums.AbsComplianceStatus
import uz.davrbank.compliance.enums.AbsHandlerStatus
import uz.davrbank.compliance.enums.AbsRequestType
import java.math.BigDecimal
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.time.Instant
import java.time.LocalDate
import java.util.Date

typealias ComplianceStatus = AbsComplianceStatus

data class AbsCheckerAdminResponse(
    val id: String,
    val request: AbsCheckerRequestDto,
    val complianceStatus: AbsComplianceStatus,
    val operationType: AbsCheckOperationType?,
    val status: AbsHandlerStatus,
    val comment: String? = null,
    val createdDate: Date,
    val checked: Boolean? = false,
    val responseListExists: Boolean? = false,
    )

data class AbsCheckerAdminGetOneResponse(
    val id: String? = null,
    val request: AbsCheckerRequestDto? = null,
    val responseListExists: Boolean? = false,
    val complianceStatus: AbsComplianceStatus? = AbsComplianceStatus.NOT_CHECKED,
    val operationType: AbsCheckOperationType? = AbsCheckOperationType.OPEN_ACCOUNT,
    val status: AbsHandlerStatus? = AbsHandlerStatus.CREATED,
    val comment: String? = null,
    val createdDate: Date? = null,
)

data class AbsCheckerResponse(
    val id: String,
)

data class CheckResultResponseWrapper(
    var status: Boolean,
    var data: AbsDataCheckerResultResponse? = null,
)

data class AbsDataCheckerResultResponse(
    var id: String,
    var status: ComplianceStatus,
    var comment: String?,
)

data class AbsCheckerRequestDto(
    val isResident: Boolean,
    val firstname: String? = null,
    val lastname: String? = null,
    val patronymic: String? = null,
    val gender: String? = null,
    val birthday: LocalDate? = null,
    val nationality: String? = null,
    val type: AbsRequestType?,
    val clientInn: String? = null,
    val orgName: String? = null,
    val country: String? = null,
    val operationType: AbsCheckOperationType?,
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

data class AbsCheckerSearchResponse(
    val took: Long,
    val total: Long,
    val maxScore: Double,
    val page: Int,
    val size: Int,
    val list: List<AbsCheckerSearchHit>,
)

data class AbsCheckerSearchHit(
    val id: String,
    val score: Double,
    val name: String? = null,
    val fullName: String? = null,
    val firstname: String? = null,
    val lastname: String? = null,
    val identificationNumber: String? = null,
    val region: String? = null,
    val birth: String? = null,
    val entityAdverseMedia: Boolean,
    val extLookUpId: String? = null,
    val type: String? = null,
    val complianceType: String? = null,
    val dataType: String? = null,
)

data class AbsApiLogResponse(
    val id: String,
    val requestId: String? = null,
    val username: String? = null,
    val header: Map<String, String>,
    val method: String,
    val uri: String,
    val urlParams: Map<String, String>,
    val requestBody: String? = null,
    val responseBody: String? = null,
    val additionals: Map<String, Any>? = null,
    val statusCode: Int,
    val encoding: String? = null,
    val createdDate: Date,
)

internal fun AbsCheckerRecord.toAdminResponse(): AbsCheckerAdminResponse = AbsCheckerAdminResponse(
    id = requireNotNull(id) { "ABS checker record id must not be null." },
    request = request.toDto(),
    complianceStatus = complianceStatus,
    operationType = operationType,
    status = status,
    comment = comment,
    createdDate = createdDate,
    responseListExists = response.responseListExists()
)

internal fun AbsCheckerRecord.toDetailResponse(): AbsCheckerAdminGetOneResponse = AbsCheckerAdminGetOneResponse(
    id = requireNotNull(id) { "ABS checker record id must not be null." },
    request = request.toDto(),
    responseListExists = response.responseListExists(),
    complianceStatus = complianceStatus,
    operationType = operationType,
    status = status,
    comment = comment,
    createdDate = createdDate,
)

internal fun AbsCheckerRequestPayload.toDto(): AbsCheckerRequestDto = AbsCheckerRequestDto(
    isResident = isResident == true,
    firstname = firstname,
    lastname = lastname,
    patronymic = patronymic,
    gender = gender,
    birthday = birthday,
    nationality = nationality,
    type = type,
    clientInn = clientInn,
    orgName = orgName,
    country = country,
    operationType = operationType,
    swiftCode = swiftCode,
    bankBik = bankBik,
    address = address,
    director = director,
    directorInn = directorInn,
    founder = founder,
    founderInn = founderInn,
    partnerName = partnerName,
    partnerInn = partnerInn,
    orgRegistrationDate = orgRegistrationDate,
    participationShare = participationShare,
    branch = branch,
    actionType = actionType,
    requestedDate = requestedDate,
    comment = comment,
)

internal fun AbsCheckerStoredSearchResponse.toDto(
    page: Int = 0,
    size: Int = hits.hits.size,
): AbsCheckerSearchResponse = AbsCheckerSearchResponse(
    took = took,
    total = hits.total,
    maxScore = hits.maxScore,
    page = page,
    size = size,
    list = hits.hits.map(AbsCheckerStoredHit::toDto),
)

internal fun AbsCheckerStoredHit.toDto(): AbsCheckerSearchHit = AbsCheckerSearchHit(
    id = id,
    score = score,
    name = name?.trim()?.ifEmpty { fullName?.trim()?.ifEmpty { firstname?.trim()?.ifEmpty { lastname} } },
    fullName = fullName,
    firstname = firstname,
    lastname = lastname,
    identificationNumber = identificationNumber,
    region = region,
    birth = birth,
    entityAdverseMedia = entityAdverseMedia,
    extLookUpId = extLookUpId,
    type = type,
    complianceType = complianceType,
    dataType = dataType,
)

private fun AbsCheckerStoredSearchResponse?.responseListExists(): Boolean =
    this?.hits?.let { storedHits ->
        storedHits.total > 0 || storedHits.hits.isNotEmpty()
    } ?: false

internal fun ApiLog.toResponse(): AbsApiLogResponse = AbsApiLogResponse(
    id = requireNotNull(id) { "API log id must not be null." }.toString(),
    requestId = requestId,
    username = username,
    header = emptyMap(),
    method = method,
    uri = endpoint,
    urlParams = queryParams.toQueryParamMap(),
    requestBody = requestBody.toJsonString(),
    responseBody = responseBody.toJsonString(),
    additionals = additional,
    statusCode = statusCode,
    encoding = StandardCharsets.UTF_8.name(),
    createdDate = Date.from(createdAt ?: Instant.now()),
)

private val apiLogResponseObjectMapper = jacksonObjectMapper()

private fun Map<String, Any>?.toJsonString(): String? =
    this?.let { value ->
        runCatching { apiLogResponseObjectMapper.writeValueAsString(value) }
            .getOrDefault(value.toString())
    }

private fun String?.toQueryParamMap(): Map<String, String> =
    this?.split("&")
        ?.filter(String::isNotBlank)
        ?.associate { param ->
            val key = param.substringBefore("=").decodeUrlParameter()
            val value = param.substringAfter("=", "").decodeUrlParameter()
            key to value
        }
        ?: emptyMap()

private fun String.decodeUrlParameter(): String =
    runCatching { URLDecoder.decode(this, StandardCharsets.UTF_8) }
        .getOrDefault(this)
