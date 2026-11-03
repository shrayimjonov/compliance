package uz.davrbank.compliance.services

import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uz.davrbank.compliance.AbsCheckOperationType
import uz.davrbank.compliance.AbsDataChecker
import uz.davrbank.compliance.AbsDataCheckerAdminGetOneResponse
import uz.davrbank.compliance.AbsDataCheckerAdminResponse
import uz.davrbank.compliance.AbsDataCheckerChangeStatusRequest
import uz.davrbank.compliance.AbsDataCheckerRepository
import uz.davrbank.compliance.AbsDataCheckerRequest
import uz.davrbank.compliance.AbsDataCheckerResponse
import uz.davrbank.compliance.AbsDataCheckerResultResponse
import uz.davrbank.compliance.AbsHandlerStatus
import uz.davrbank.compliance.ApiLog
import uz.davrbank.compliance.ApiLogRepository
import uz.davrbank.compliance.CheckResultResponseWrapper
import uz.davrbank.compliance.ComplianceStatus
import uz.davrbank.compliance.GeneralApiException
import java.util.Date

interface AbsDataCheckerService {
    fun getAllData(
        pageable: Pageable,
        status: ComplianceStatus?,
        operationType: AbsCheckOperationType?,
        startDate: Long?,
        endDate: Long?,
        isResident: Boolean?
    ): Page<AbsDataCheckerAdminResponse>


    fun getOne(id: String, pageable: Pageable): AbsDataCheckerAdminGetOneResponse

    fun checkStatus(id: String): CheckResultResponseWrapper

    fun changeStatus(id: String, request: AbsDataCheckerChangeStatusRequest): AbsDataCheckerAdminResponse

    fun getLogs(pageable: Pageable): Page<ApiLog>

    fun saveData(request: AbsDataCheckerRequest): AbsDataCheckerResponse
}


@Service
class AbsDataCheckerServiceImpl(
    private val apiLogRepository: ApiLogRepository,
    private val repository: AbsDataCheckerRepository,
    private val mongoTemplate: MongoTemplate,
    private val absValidationService: AbsValidationService
) : AbsDataCheckerService {
    private val logger = LoggerFactory.getLogger(this.javaClass)
    override fun getAllData(
        pageable: Pageable,
        status: ComplianceStatus?,
        operationType: AbsCheckOperationType?,
        startDate: Long?,
        endDate: Long?,
        isResident: Boolean?
    ): Page<AbsDataCheckerAdminResponse> {
        val query = Query()
        query.fields().slice("response.hits.hits", 0, 20)
        query.limit(pageable.pageSize)
        query.skip(pageable.offset)
        query.with(Sort.by(Sort.Direction.DESC, "createdDate"))

        val criteria = mutableListOf<Criteria>()


        criteria.add(Criteria.where("status").`is`(AbsHandlerStatus.FINISHED))


        status?.let {
            criteria.add(Criteria.where("complianceStatus").`is`(it))
        }

        operationType?.let {
            criteria.add(Criteria.where("operationType").`is`(it))
        }

        if (isResident != null) {
            criteria.add(Criteria.where("request.isResident").`is`(isResident))
        }

        if (startDate != null || endDate != null) {
            val dateCriteria = Criteria.where("createdDate")
            if (startDate != null) {
                dateCriteria.gte(Date(startDate))
            }
            if (endDate != null) {
                dateCriteria.lte(Date(endDate))
            }
            criteria.add(dateCriteria)
        }

        if (criteria.isNotEmpty()) {
            query.addCriteria(Criteria().andOperator(*criteria.toTypedArray()))
        }

        val items = mongoTemplate.find(query, AbsDataChecker::class.java).map { AbsDataCheckerAdminResponse.toDto(it) }

        val total = mongoTemplate.count(query.skip(-1).limit(-1), AbsDataChecker::class.java)

        return PageImpl(items, pageable, total)
    }

    override fun getOne(id: String, pageable: Pageable): AbsDataCheckerAdminGetOneResponse {
        val query = Query(Criteria.where("_id").`is`(id))
        query.fields().slice("response.hits.hits", pageable.offset.toInt(), pageable.pageSize)


         val result = mongoTemplate.findOne(query, AbsDataChecker::class.java)
             ?: throw GeneralApiException("DATA NOT FOUND: $id")

        return AbsDataCheckerAdminGetOneResponse.toDto(result)

    }

    override fun checkStatus(id: String): CheckResultResponseWrapper {
        val absData = repository.findById(id).orElse(null) ?: return CheckResultResponseWrapper(false)
        return CheckResultResponseWrapper(
            true,
            AbsDataCheckerResultResponse(absData.id!!, absData.complianceStatus, absData.comment)
        )
    }

    @Transactional
    override fun changeStatus(id: String, request: AbsDataCheckerChangeStatusRequest): AbsDataCheckerAdminResponse {
        val data = repository.findById(id).orElseThrow { GeneralApiException("DATA NOT FOUND: $id") }

        if (data.complianceStatus != ComplianceStatus.NOT_CHECKED) {
            throw GeneralApiException("Request already processed - status changes not allowed")
        }

        data.complianceStatus = request.status
        data.comment = request.comment


        return AbsDataCheckerAdminResponse.toDto(repository.save(data))
    }

    override fun getLogs(pageable: Pageable): Page<ApiLog> {
        return apiLogRepository.findAll(pageable)
    }

    @Transactional
    override fun saveData(request: AbsDataCheckerRequest): AbsDataCheckerResponse {
        logger.info("Saving ABS check request: ${request.type}")
        absValidationService.validateRequest(request)

        val absDataChecker = AbsDataChecker(request, operationType = request.operationType)
        val saved = repository.save(absDataChecker)

        return AbsDataCheckerResponse(saved.id!!)
    }

}