package uz.davrbank.compliance.service.impl

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.persistence.EntityManager
import jakarta.persistence.Tuple
import jakarta.persistence.criteria.CriteriaBuilder
import jakarta.persistence.criteria.Order
import jakarta.persistence.criteria.Predicate
import jakarta.persistence.criteria.Root
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.repository.findByIdOrNull
import org.springframework.data.support.PageableExecutionUtils
import org.springframework.http.ResponseEntity
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.support.TransactionTemplate
import uz.davrbank.compliance.domain.ApiLog
import uz.davrbank.compliance.dto.request.filter.ApiLogListFilter
import uz.davrbank.compliance.dto.request.filter.BaseFilter
import uz.davrbank.compliance.dto.response.ApiLogResponse
import uz.davrbank.compliance.enums.ApiLogStatus
import uz.davrbank.compliance.enums.LogSource
import uz.davrbank.compliance.exception.BadRequestException
import uz.davrbank.compliance.exception.NotFoundException
import uz.davrbank.compliance.integration.davrhub.DavrHubCommunicationException
import uz.davrbank.compliance.integration.davrhub.DavrHubLexisService
import uz.davrbank.compliance.integration.davrhub.LexisRequest
import uz.davrbank.compliance.repository.ApiLogRepository
import uz.davrbank.compliance.service.ApiLogEvent
import uz.davrbank.compliance.service.ApiLogService
import java.nio.charset.StandardCharsets
import java.time.ZoneOffset
import java.util.*

@Service
class ApiLogServiceImpl(
    private val apiLogRepository: ApiLogRepository,
    private val entityManager: EntityManager,
    private val objectMapper: ObjectMapper,
    private val davrHubLexisService: DavrHubLexisService,
    transactionManager: PlatformTransactionManager,
) : ApiLogService {

    private val mapType = object : TypeReference<Map<String, Any>>() {}

    private val transactionTemplate = TransactionTemplate(transactionManager).apply {
        propagationBehavior = Propagation.REQUIRES_NEW.value()
    }

    @Async("logExecutor")
    override fun save(log: ApiLog) {
        runCatching {
            transactionTemplate.executeWithoutResult {
                apiLogRepository.save(log)
            }
        }
    }

    @Async("logExecutor")
    override fun save(event: ApiLogEvent) {
        runCatching {
            val log = event.toEntity()
            transactionTemplate.executeWithoutResult {
                apiLogRepository.save(log)
            }
        }
    }

    @Transactional(readOnly = true)
    override fun getList(filter: BaseFilter<ApiLogListFilter>): Page<ApiLogResponse> {
        val params = filter.params ?: ApiLogListFilter()
        val pagination = requireNotNull(filter.pagination) { "Property 'pagination' must be provided." }
        val pageable = pagination.toApiLogPageable()
        val criteriaBuilder = entityManager.criteriaBuilder

        val query = criteriaBuilder.createTupleQuery()
        val root = query.from(ApiLog::class.java)
        val predicates = buildPredicates(criteriaBuilder, root, filter.keyword, params)

        query.select(
            criteriaBuilder.tuple(
                root.get<UUID>(ID_FIELD).alias(ID_FIELD),
                root.get<String>(REQUEST_ID_FIELD).alias(REQUEST_ID_FIELD),
                root.get<String?>(USERNAME_FIELD).alias(USERNAME_FIELD),
                root.get<String>(METHOD_FIELD).alias(METHOD_FIELD),
                root.get<String>(ENDPOINT_FIELD).alias(ENDPOINT_FIELD),
                root.get<String?>(QUERY_PARAMS_FIELD).alias(QUERY_PARAMS_FIELD),
                root.get<Map<String, Any>?>(REQUEST_BODY_FIELD).alias(REQUEST_BODY_FIELD),
                criteriaBuilder.selectCase<Boolean>()
                    .`when`(criteriaBuilder.isNotNull(root.get<Map<String, Any>?>(RESPONSE_BODY_FIELD)), true)
                    .otherwise(false)
                    .alias(RESPONSE_LIST_EXISTS_FIELD),
                root.get<Map<String, Any>?>(ADDITIONAL_FIELD).alias(ADDITIONAL_FIELD),
                root.get<LogSource>(SOURCE_FIELD).alias(SOURCE_FIELD),
                root.get<ApiLogStatus>(STATUS_FIELD).alias(STATUS_FIELD),
                root.get<Int>(STATUS_CODE_FIELD).alias(STATUS_CODE_FIELD),
                root.get<Long>(TIME_TAKEN_MS_FIELD).alias(TIME_TAKEN_MS_FIELD),
                root.get<java.time.Instant?>(CREATED_AT_FIELD).alias(CREATED_AT_FIELD),
            )
        )
        if (predicates.isNotEmpty()) {
            query.where(*predicates.toTypedArray())
        }
        query.orderBy(buildOrders(criteriaBuilder, root, pageable.sort))

        val items = entityManager.createQuery(query)
            .setFirstResult(pageable.offset.toFirstResult())
            .setMaxResults(pageable.pageSize)
            .resultList
            .map { tuple -> tuple.toApiLogResponse() }

        return PageableExecutionUtils.getPage(items, pageable) {
            count(filter.keyword, params)
        }
    }

    @Transactional(readOnly = true)
    override fun getResponse(id: UUID): Map<String, Any>? = findResponse(id, source = null)

    @Transactional(readOnly = true)
    override fun getResponse(id: UUID, source: LogSource): Map<String, Any>? = findResponse(id, source)

    @Transactional(readOnly = true)
    override fun getResponseByRequestId(requestId: String, source: LogSource): Map<String, Any>? {
        val log = apiLogRepository.findFirstByRequestIdAndSourceOrderByCreatedAtDesc(requestId, source)
            ?: throw NotFoundException("API log not found for requestId=$requestId")

        return log.responseBody
    }

    @Transactional(readOnly = true)
    override fun existingResponseRequestIds(requestIds: Collection<String>, source: LogSource): Set<String> {
        if (requestIds.isEmpty()) return emptySet()
        return apiLogRepository.findRequestIdsWithResults(requestIds, source.name).toSet()
    }

    override fun updateStatus(id: UUID): ApiLogResponse {
        val log = apiLogRepository.findByIdOrNull(id)
            ?: throw NotFoundException("API log not found for id=$id")

        validateStatusUpdate(log)

        val davrHubAttemptId = UUID.randomUUID().toString()
        val davrHubRequest = LexisRequest(requestId = log.requestId)

        return try {
            val davrHubResponse = davrHubLexisService.markAsChecked(davrHubRequest)

            log.requestBody = log.requestBody.withPayload(DAVRHUB_REQUEST_FIELD + davrHubAttemptId, davrHubRequest)
            log.responseBody = log.responseBody.withPayload(DAVRHUB_RESPONSE_FIELD + davrHubAttemptId, davrHubResponse)
            log.status = ApiLogStatus.CHECKED

            apiLogRepository.save(log).toApiLogResponse()
        } catch (ex: Exception) {
            log.requestBody = log.requestBody.withPayload(DAVRHUB_REQUEST_FIELD + davrHubAttemptId, davrHubRequest)
            log.responseBody = log.responseBody.withPayload(DAVRHUB_RESPONSE_FIELD + davrHubAttemptId, ex.toDavrHubErrorPayload())
            apiLogRepository.save(log)

            throw ex
        }
    }

    private fun validateStatusUpdate(log: ApiLog) {
        if (log.source != LogSource.NCI) {
            throw BadRequestException("Only NCI API logs can be checked.")
        }

        if (log.status == ApiLogStatus.CHECKED) {
            throw BadRequestException("API log is already checked.")
        }
    }

    private fun ApiLogEvent.toEntity(): ApiLog = ApiLog(
        requestId = requestId,
        username = username,
        method = method,
        endpoint = endpoint,
        queryParams = queryParams,
        requestBody = requestBody.toLogMapOrNull(),
        responseBody = responseBody.toLogMapOrNull(),
        source = source,
        statusCode = statusCode,
        timeTakenMs = timeTakenMs,
    )

    private fun Map<String, Any>?.withPayload(key: String, payload: Any): Map<String, Any> =
        (this ?: emptyMap()) + (key to payload.toLogMap())

    private fun Any.toLogMap(): Map<String, Any> =
        toLogMapOrNull() ?: emptyMap()

    private fun Any?.toLogMapOrNull(): Map<String, Any>? =
        when (this) {
            null -> null
            is ResponseEntity<*> -> body.toLogMapOrNull()
            is Map<*, *> -> entries.associate { (key, value) -> key.toString() to (value ?: "") }
            is String -> toLogMapOrNull()
            is ByteArray -> toString(StandardCharsets.UTF_8).toLogMapOrNull()
            else -> runCatching {
                objectMapper.valueToTree<JsonNode>(this).toLogMapOrNull()
            }.getOrElse {
                toString().takeIf(String::isNotBlank)?.let { raw -> mapOf(RAW_FIELD to raw) }
            }
        }

    private fun String.toLogMapOrNull(): Map<String, Any>? {
        val raw = trim()
        if (raw.isBlank()) {
            return null
        }

        return runCatching {
            objectMapper.readTree(raw).toLogMapOrNull()
        }.getOrElse {
            mapOf(RAW_FIELD to raw)
        }
    }

    private fun JsonNode.toLogMapOrNull(): Map<String, Any>? =
        when {
            isNull || isMissingNode -> null
            isObject -> objectMapper.convertValue(this, mapType)
            else -> mapOf(VALUE_FIELD to objectMapper.convertValue(this, Any::class.java))
        }

    private fun Exception.toDavrHubErrorPayload(): Map<String, Any> {
        val payload = linkedMapOf<String, Any>(
            ERROR_FIELD to (message ?: javaClass.simpleName),
            EXCEPTION_FIELD to javaClass.simpleName,
        )

        if (this is DavrHubCommunicationException) {
            downstreamStatusCode?.let { statusCode ->
                payload[DOWNSTREAM_STATUS_FIELD] = statusCode.value()
            }
            responseBody
                ?.trim()
                ?.takeIf(String::isNotBlank)
                ?.let { rawBody ->
                    payload[RESPONSE_BODY_FIELD] = rawBody.toLogMap()
                }
        }

        return payload
    }

    private fun findResponse(id: UUID, source: LogSource?): Map<String, Any>? {
        val log = if (source == null) {
            apiLogRepository.findByIdOrNull(id)
        } else {
            apiLogRepository.findByIdAndSource(id, source)
        }
            ?: throw NotFoundException("API log not found for id=$id")

        return log.responseBody
    }

    private fun count(keyword: String?, params: ApiLogListFilter): Long {
        val criteriaBuilder = entityManager.criteriaBuilder
        val countQuery = criteriaBuilder.createQuery(Long::class.java)
        val root = countQuery.from(ApiLog::class.java)
        val predicates = buildPredicates(criteriaBuilder, root, keyword, params)

        countQuery.select(criteriaBuilder.count(root))
        if (predicates.isNotEmpty()) {
            countQuery.where(*predicates.toTypedArray())
        }

        return entityManager.createQuery(countQuery).singleResult
    }

    private fun buildPredicates(
        criteriaBuilder: CriteriaBuilder,
        root: Root<ApiLog>,
        keyword: String?,
        params: ApiLogListFilter,
    ): List<Predicate> {
        val predicates = mutableListOf<Predicate>()

        params.source?.let { source ->
            predicates += criteriaBuilder.equal(root.get<LogSource>(SOURCE_FIELD), source)
        }

        params.method.trimToNull()?.let { method ->
            predicates += criteriaBuilder.equal(root.get<String>(METHOD_FIELD), method.uppercase(Locale.ROOT))
        }

        params.statusCode?.let { statusCode ->
            predicates += criteriaBuilder.equal(root.get<Int>(STATUS_CODE_FIELD), statusCode)
        }

        params.createdDate?.let { createdDate ->
            val start = createdDate.atStartOfDay(ZoneOffset.UTC).toInstant()
            val endExclusive = createdDate.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant()
            predicates += criteriaBuilder.greaterThanOrEqualTo(root.get(CREATED_AT_FIELD), start)
            predicates += criteriaBuilder.lessThan(root.get(CREATED_AT_FIELD), endExclusive)
        }

        keyword.trimToNull()?.let { value ->
            val keywordPattern = value.lowercase(Locale.ROOT).containsPattern()
            predicates += criteriaBuilder.or(
                *KEYWORD_FIELDS.map { field ->
                    criteriaBuilder.like(
                        criteriaBuilder.lower(root.get(field)),
                        keywordPattern,
                        LIKE_ESCAPE,
                    )
                }.toTypedArray()
            )
        }

        return predicates
    }

    private fun buildOrders(
        criteriaBuilder: CriteriaBuilder,
        root: Root<ApiLog>,
        sort: Sort,
    ): List<Order> {
        val orders = sort.map { order ->
            val fieldName = SORT_FIELDS[order.property] ?: DEFAULT_SORT_FIELD
            if (order.isAscending) {
                criteriaBuilder.asc(root.get<Comparable<Any>>(fieldName))
            } else {
                criteriaBuilder.desc(root.get<Comparable<Any>>(fieldName))
            }
        }.toMutableList()

        val hasCreatedAtOrder = sort.any { order -> SORT_FIELDS[order.property] == CREATED_AT_FIELD }
        if (!hasCreatedAtOrder) {
            orders += criteriaBuilder.desc(root.get<java.time.Instant>(CREATED_AT_FIELD))
        }

        return orders
    }

    private fun BaseFilter.Pagination.toApiLogPageable(): Pageable {
        val boundedPage = page.coerceAtLeast(0)
        val boundedSize = size.coerceIn(1, MAX_PAGE_SIZE)
        val requestedSort = orderBy?.trim().orEmpty()
        val sortField = SORT_FIELDS[requestedSort] ?: DEFAULT_SORT_FIELD
        val direction = if (sortOrder?.equals(SORT_ASC, ignoreCase = true) == true) {
            Sort.Direction.ASC
        } else {
            Sort.Direction.DESC
        }

        return PageRequest.of(boundedPage, boundedSize, Sort.by(direction, sortField))
    }

    private fun Tuple.toApiLogResponse(): ApiLogResponse = ApiLogResponse(
        id = get(ID_FIELD, UUID::class.java),
        requestId = get(REQUEST_ID_FIELD, String::class.java),
        username = get(USERNAME_FIELD, String::class.java),
        method = get(METHOD_FIELD, String::class.java),
        endpoint = get(ENDPOINT_FIELD, String::class.java),
        queryParams = get(QUERY_PARAMS_FIELD, String::class.java),
        requestBody = getMap(REQUEST_BODY_FIELD),
        responseListExists = get(RESPONSE_LIST_EXISTS_FIELD, java.lang.Boolean::class.java).booleanValue(),
        additional = getMap(ADDITIONAL_FIELD),
        source = get(SOURCE_FIELD, LogSource::class.java),
        status = get(STATUS_FIELD, ApiLogStatus::class.java),
        statusCode = get(STATUS_CODE_FIELD, Integer::class.java).toInt(),
        timeTakenMs = get(TIME_TAKEN_MS_FIELD, java.lang.Long::class.java).toLong(),
        createdDate = get(CREATED_AT_FIELD, java.time.Instant::class.java),
    )

    private fun ApiLog.toApiLogResponse(): ApiLogResponse = ApiLogResponse(
        id = requireNotNull(id),
        requestId = requestId,
        username = username,
        method = method,
        endpoint = endpoint,
        queryParams = queryParams,
        requestBody = requestBody,
        responseListExists = responseBody != null,
        additional = additional,
        status = status,
        statusCode = statusCode,
        timeTakenMs = timeTakenMs,
        createdDate = createdAt,
        source = source,
    )

    @Suppress("UNCHECKED_CAST")
    private fun Tuple.getMap(alias: String): Map<String, Any>? = get(alias) as? Map<String, Any>

    private fun String?.trimToNull(): String? = this?.trim()?.takeIf(String::isNotBlank)

    private fun String.containsPattern(): String =
        "%${replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_")}%"

    private fun Long.toFirstResult(): Int = coerceAtMost(Int.MAX_VALUE.toLong()).toInt()

    companion object {
        private const val MAX_PAGE_SIZE = 100
        private const val SORT_ASC = "asc"
        private const val LIKE_ESCAPE = '\\'

        private const val ID_FIELD = "id"
        private const val REQUEST_ID_FIELD = "requestId"
        private const val USERNAME_FIELD = "username"
        private const val METHOD_FIELD = "method"
        private const val ENDPOINT_FIELD = "endpoint"
        private const val QUERY_PARAMS_FIELD = "queryParams"
        private const val REQUEST_BODY_FIELD = "requestBody"
        private const val RESPONSE_BODY_FIELD = "responseBody"
        private const val RESPONSE_LIST_EXISTS_FIELD = "responseListExists"
        private const val ADDITIONAL_FIELD = "additional"
        private const val SOURCE_FIELD = "source"
        private const val STATUS_FIELD = "status"
        private const val STATUS_CODE_FIELD = "statusCode"
        private const val TIME_TAKEN_MS_FIELD = "timeTakenMs"
        private const val CREATED_AT_FIELD = "createdAt"
        private const val DAVRHUB_REQUEST_FIELD = "davrHubRequest_"
        private const val DAVRHUB_RESPONSE_FIELD = "davrHubResponse_"
        private const val ERROR_FIELD = "error"
        private const val EXCEPTION_FIELD = "exception"
        private const val DOWNSTREAM_STATUS_FIELD = "downstreamStatus"
        private const val RAW_FIELD = "raw"
        private const val VALUE_FIELD = "value"
        private const val CREATED_DATE_SORT = "createdDate"
        private const val CREATED_AT_SORT = "createdAt"
        private const val STATUS_CODE_ALIAS = "statuscode"
        private const val STATUS_CODE_SNAKE_ALIAS = "status_code"
        private const val TIME_TAKEN_MS_ALIAS = "timeTaken"
        private const val DEFAULT_SORT_FIELD = CREATED_AT_FIELD

        private val KEYWORD_FIELDS = listOf(REQUEST_ID_FIELD, USERNAME_FIELD, ENDPOINT_FIELD)

        private val SORT_FIELDS = mapOf(
            ID_FIELD to ID_FIELD,
            REQUEST_ID_FIELD to REQUEST_ID_FIELD,
            USERNAME_FIELD to USERNAME_FIELD,
            METHOD_FIELD to METHOD_FIELD,
            ENDPOINT_FIELD to ENDPOINT_FIELD,
            SOURCE_FIELD to SOURCE_FIELD,
            STATUS_FIELD to STATUS_FIELD,
            STATUS_CODE_FIELD to STATUS_CODE_FIELD,
            STATUS_CODE_ALIAS to STATUS_CODE_FIELD,
            STATUS_CODE_SNAKE_ALIAS to STATUS_CODE_FIELD,
            TIME_TAKEN_MS_FIELD to TIME_TAKEN_MS_FIELD,
            TIME_TAKEN_MS_ALIAS to TIME_TAKEN_MS_FIELD,
            CREATED_DATE_SORT to CREATED_AT_FIELD,
            CREATED_AT_SORT to CREATED_AT_FIELD,
        )
    }
}
