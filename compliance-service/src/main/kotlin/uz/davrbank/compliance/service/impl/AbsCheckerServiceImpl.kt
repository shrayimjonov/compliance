package uz.davrbank.compliance.service.impl

import mu.KotlinLogging
import org.springframework.data.domain.*
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import uz.davrbank.compliance.domain.AbsCheckerRecord
import uz.davrbank.compliance.dto.request.abs.AbsCheckerChangeStatusRequest
import uz.davrbank.compliance.dto.request.abs.AbsCheckerRequest
import uz.davrbank.compliance.dto.request.filter.AbsCheckerListFilter
import uz.davrbank.compliance.dto.request.filter.BaseFilter
import uz.davrbank.compliance.dto.response.abs.*
import uz.davrbank.compliance.enums.AbsComplianceStatus
import uz.davrbank.compliance.enums.AbsHandlerStatus
import uz.davrbank.compliance.exception.BadRequestException
import uz.davrbank.compliance.exception.NotFoundException
import uz.davrbank.compliance.repository.AbsCheckerRepository
import uz.davrbank.compliance.repository.ApiLogRepository
import uz.davrbank.compliance.service.AbsCheckerService

@Service
class AbsCheckerServiceImpl(
    private val apiLogRepository: ApiLogRepository,
    private val repository: AbsCheckerRepository,
    private val mongoTemplate: MongoTemplate,
    private val absCheckerRequestValidator: AbsCheckerRequestValidator,
    private val absCheckerAsyncProcessor: AbsCheckerAsyncProcessor,
) : AbsCheckerService {

    override fun getList(filter: BaseFilter<AbsCheckerListFilter>): Page<AbsCheckerAdminResponse> {
        val params = filter.params ?: AbsCheckerListFilter()
        val pagination = requireNotNull(filter.pagination) { "Property 'pagination' must be provided." }
        val criteria = buildListCriteria(params)
        val effectivePageable = pagination.toPageable(MAX_PAGE_SIZE)

        val query = Query(criteria)
            .limit(effectivePageable.pageSize)
            .skip(effectivePageable.offset)
            .with(effectivePageable.sort)
        query.fields().slice(RESPONSE_HITS_FIELD, 0, RESPONSE_EXISTS_PREVIEW_LIMIT)

        val items = mongoTemplate.find(query, AbsCheckerRecord::class.java)
            .map(AbsCheckerRecord::toAdminResponse)
        val total = mongoTemplate.count(Query(criteria), AbsCheckerRecord::class.java)

        return PageImpl(items, effectivePageable, total)
    }

    override fun get(id: String): AbsCheckerAdminGetOneResponse {
        val query = Query(Criteria.where(ID_FIELD).`is`(id))
        query.fields().slice(RESPONSE_HITS_FIELD, 0, RESPONSE_EXISTS_PREVIEW_LIMIT)

        return findRecord(id, query).toDetailResponse()
    }

    override fun getResponse(id: String, pageable: Pageable): AbsCheckerSearchResponse? {
        val page = if (pageable.isPaged) pageable.pageNumber.coerceAtLeast(0) else 0
        val size = if (pageable.isPaged) pageable.pageSize.coerceAtLeast(1) else DEFAULT_RESPONSE_PAGE_SIZE
        val offset = (page.toLong() * size)
            .coerceAtMost(Int.MAX_VALUE.toLong())
            .toInt()
        val query = Query(Criteria.where(ID_FIELD).`is`(id))
        query.fields().slice(
            RESPONSE_HITS_FIELD,
            offset,
            size,
        )

        return findRecord(id, query).response?.toDto(
            page = page,
            size = size,
        )
    }

    override fun checkStatus(id: String): AbsCheckerAdminGetOneResponse {
        val absData = repository.findByIdOrNull(id) ?: return AbsCheckerAdminGetOneResponse()

        return absData.toDetailResponse()
    }

    override fun updateStatus(id: String, request: AbsCheckerChangeStatusRequest): AbsCheckerAdminResponse {
        val data = repository.findByIdOrNull(id)
            ?: throw NotFoundException("ABS checker request not found for id=$id")

        if (data.status != AbsHandlerStatus.FINISHED) {
            throw BadRequestException("ABS checker request has not finished processing yet.")
        }

        if (data.complianceStatus != AbsComplianceStatus.NOT_CHECKED) {
            throw BadRequestException("Request already processed - status changes not allowed.")
        }

        data.complianceStatus = request.status
            ?: throw BadRequestException("Property 'status' must be provided.")
        data.comment = request.comment?.trim()?.takeIf(String::isNotBlank)

        return repository.save(data).toAdminResponse()
    }

    override fun getLogs(pageable: Pageable): Page<AbsApiLogResponse> {
        val effectivePageable = PageRequest.of(
            pageable.pageNumber.coerceAtLeast(0),
            pageable.pageSize.coerceAtLeast(1),
            Sort.by(Sort.Direction.DESC, CREATED_AT_FIELD),
        )

        return apiLogRepository.findAll(effectivePageable)
            .map { it.toResponse() }
    }

    override fun create(request: AbsCheckerRequest): AbsCheckerResponse {
        val payload = absCheckerRequestValidator.validateAndBuildPayload(request)

        logger.info {
            "Saving ABS checker request type='${payload.type?.name}', operationType='${payload.operationType?.name}'"
        }

        val saved = repository.save(
            AbsCheckerRecord(
                request = payload,
                operationType = payload.operationType,
            )
        )

        val savedId = requireNotNull(saved.id)
        triggerAsyncProcessing(savedId)

        return AbsCheckerResponse(savedId)
    }

    private fun triggerAsyncProcessing(recordId: String) {
        try {
            absCheckerAsyncProcessor.processRecord(recordId)
        } catch (ex: Exception) {
            logger.warn(ex) {
                "Unable to dispatch async ABS checker processing for recordId='$recordId'. Record will stay retryable."
            }
        }
    }

    private fun buildListCriteria(filter: AbsCheckerListFilter): Criteria {
        val criteria = mutableListOf<Criteria>()
        criteria += Criteria.where(STATUS_FIELD).`is`(AbsHandlerStatus.FINISHED)

        filter.status?.let {
            criteria += Criteria.where(COMPLIANCE_STATUS_FIELD).`is`(it)
        }

        filter.operationType?.let {
            criteria += Criteria.where(OPERATION_TYPE_FIELD).`is`(it)
        }

        filter.isResident?.let {
            criteria += Criteria.where(REQUEST_IS_RESIDENT_FIELD).`is`(it)
        }

        if (filter.startDate != null || filter.endDate != null) {
            val dateCriteria = Criteria.where(CREATED_DATE_FIELD)
            filter.startDate?.let(dateCriteria::gte)
            filter.endDate?.let(dateCriteria::lte)
            criteria += dateCriteria
        }

        return if (criteria.size == 1) {
            criteria.first()
        } else {
            Criteria().andOperator(*criteria.toTypedArray())
        }
    }

    private fun findRecord(id: String, query: Query): AbsCheckerRecord =
        mongoTemplate.findOne(query, AbsCheckerRecord::class.java)
            ?: throw NotFoundException("ABS checker request not found for id=$id")

    companion object {
        private val logger = KotlinLogging.logger {}

        private const val ID_FIELD = "_id"
        private const val STATUS_FIELD = "status"
        private const val COMPLIANCE_STATUS_FIELD = "complianceStatus"
        private const val OPERATION_TYPE_FIELD = "operationType"
        private const val REQUEST_IS_RESIDENT_FIELD = "request.isResident"
        private const val CREATED_DATE_FIELD = "createdDate"
        private const val CREATED_AT_FIELD = "createdAt"
        private const val RESPONSE_HITS_FIELD = "response.hits.hits"
        private const val RESPONSE_EXISTS_PREVIEW_LIMIT = 1
        private const val DEFAULT_RESPONSE_PAGE_SIZE = 20
        private const val MAX_PAGE_SIZE = 100
    }
}
