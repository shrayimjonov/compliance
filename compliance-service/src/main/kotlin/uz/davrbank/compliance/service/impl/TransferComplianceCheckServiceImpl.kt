package uz.davrbank.compliance.service.impl

import jakarta.persistence.criteria.Predicate
import mu.KotlinLogging
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.domain.Specification
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.support.TransactionSynchronization
import org.springframework.transaction.support.TransactionSynchronizationManager
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import uz.davrbank.compliance.domain.TransferComplianceCheck
import uz.davrbank.compliance.dto.request.filter.BaseFilter
import uz.davrbank.compliance.dto.request.filter.TransferComplianceCheckListFilter
import uz.davrbank.compliance.dto.request.filter.TransferComplianceCheckReportRequest
import uz.davrbank.compliance.dto.request.transfer.TransferComplianceCheckCreateRequest
import uz.davrbank.compliance.dto.request.transfer.TransferComplianceCheckStatusUpdateRequest
import uz.davrbank.compliance.dto.request.transfer.TransferComplianceCheckUpdateRequest
import uz.davrbank.compliance.dto.response.FileDetailDTO
import uz.davrbank.compliance.dto.response.transfer.TransferComplianceCheckDetailDTO
import uz.davrbank.compliance.dto.response.transfer.TransferComplianceCheckResponse
import uz.davrbank.compliance.dto.response.transfer.toDetailDTO
import uz.davrbank.compliance.dto.response.transfer.toResponse
import uz.davrbank.compliance.enums.AttachmentEntityType
import uz.davrbank.compliance.enums.GoodsType
import uz.davrbank.compliance.enums.LogSource
import uz.davrbank.compliance.enums.TransferComplianceStatus
import uz.davrbank.compliance.exception.BadRequestException
import uz.davrbank.compliance.exception.NotFoundException
import uz.davrbank.compliance.report.TransferComplianceCheckReportGenerator
import uz.davrbank.compliance.repository.TransferComplianceCheckRepository
import uz.davrbank.compliance.service.ApiLogService
import uz.davrbank.compliance.service.AttachmentService
import uz.davrbank.compliance.service.TransferComplianceCheckService
import java.time.LocalDateTime
import java.util.*

@Service
class TransferComplianceCheckServiceImpl(
    private val repository: TransferComplianceCheckRepository,
    private val attachmentService: AttachmentService,
    private val tifProcessor: TransferComplianceTifProcessor,
    private val apiLogService: ApiLogService,
    private val reportGenerator: TransferComplianceCheckReportGenerator,
) : TransferComplianceCheckService {

    @Transactional(readOnly = true)
    override fun getList(
        filter: BaseFilter<TransferComplianceCheckListFilter>,
    ): Page<TransferComplianceCheckResponse> = list(filter, allowedStatuses = null)

    @Transactional(readOnly = true)
    override fun getExternalList(
        filter: BaseFilter<TransferComplianceCheckListFilter>,
    ): Page<TransferComplianceCheckResponse> = list(filter, allowedStatuses = EXTERNAL_VISIBLE_STATUSES)

    @Transactional(readOnly = true)
    override fun downloadExternalReport(request: TransferComplianceCheckReportRequest): ByteArray {
        val params = request.params ?: TransferComplianceCheckListFilter()
        val specification = buildSpecification(request.keyword, params, EXTERNAL_VISIBLE_STATUSES)
        val sort = Sort.by(Sort.Direction.DESC, CREATED_DATE_FIELD)

        val entities = repository.findAll(specification, sort)
        val checkableIds = entities
            .filter { it.status !in RESPONSE_EXCLUDED_STATUSES }
            .mapNotNull { it.id }
        val idsWithTifResponse = apiLogService.existingResponseRequestIds(
            checkableIds.map(UUID::toString),
            LogSource.TIF,
        )

        val rows = entities.map { entity ->
            entity.toResponse(responseListExists = entity.id?.toString() in idsWithTifResponse)
        }
        return reportGenerator.generate(rows)
    }

    private fun list(
        filter: BaseFilter<TransferComplianceCheckListFilter>,
        allowedStatuses: Collection<TransferComplianceStatus>?,
    ): Page<TransferComplianceCheckResponse> {
        val params = filter.params ?: TransferComplianceCheckListFilter()
        val pagination = requireNotNull(filter.pagination) { "Property 'pagination' must be provided." }
        val pageable = pagination.toPageable()
        val specification = buildSpecification(filter.keyword, params, allowedStatuses)

        val page = repository.findAll(specification, pageable)
        val entityIds = page.content.mapNotNull { it.id }
        val filesMap = attachmentService.getByEntities(entityIds, ENTITY_TYPE)

        val checkableIds = page.content
            .filter { it.status !in RESPONSE_EXCLUDED_STATUSES }
            .mapNotNull { it.id }
        val idsWithTifResponse = apiLogService.existingResponseRequestIds(
            checkableIds.map(UUID::toString),
            LogSource.TIF,
        )

        return page.map { entity ->
            val files = filesMap[entity.id] ?: emptyList()
            entity.toResponse(
                files = files,
                responseListExists = entity.id?.toString() in idsWithTifResponse,
            )
        }
    }

    @Transactional
    override fun delete(id: UUID): UUID {
        val entity = repository.findById(id).orElseThrow {
            NotFoundException("Transfer compliance check not found for id=$id")
        }
        repository.delete(entity)
        return requireNotNull(entity.id)
    }

    @Transactional(readOnly = true)
    override fun getByExternalId(externalId: UUID): TransferComplianceCheckDetailDTO {
        val entity = repository.findByExternalId(externalId)
            ?: throw NotFoundException("Transfer compliance check not found for externalId=$externalId")
        val files = attachmentService.getByEntity(requireNotNull(entity.id), ENTITY_TYPE)
        return entity.toDetailDTO(files)
    }

    @Transactional
    override fun create(request: TransferComplianceCheckCreateRequest): TransferComplianceCheckResponse {
        val entity = TransferComplianceCheck(
            checkNumber = nextCheckNumber(),
            externalId = UUID.randomUUID(),
            branchCode = request.branchCode.trim(),
            clientName = request.clientName.trim(),
            contractTypeCode = request.contractTypeCode.trim(),
            contractNumber = request.contractNumber.trim(),
            clientTin = request.clientTin.trimToNull(),
            enterpriseRegistrationDate = request.enterpriseRegistrationDate,
            activityType = request.activityType.trimToNull(),
            contractDate = request.contractDate,
            contractIdn = request.contractIdn.trimToNull(),
            foreignPartnerName = request.foreignPartnerName.trimToNull(),
            foreignPartnerTin = request.foreignPartnerTin.trimToNull(),
            directorAndRelatedParties = request.directorAndRelatedParties.trimToNull(),
            foreignPartnerCountryCode = request.foreignPartnerCountryCode.trimToNull(),
            foreignPartnerBank = request.foreignPartnerBank.trimToNull(),
            bicSwift = request.bicSwift.trimToNull(),
            foreignPartnerBankCountryCode = request.foreignPartnerBankCountryCode.trimToNull(),
            goodsType = request.goodsType,
            goodsName = request.goodsName.trimToNull(),
            goodsTnvedCode = request.goodsTnvedCode.trimToNull(),
            goodsOriginCountryCode = request.goodsOriginCountryCode.trimToNull(),
            consignor = request.consignor.trimToNull(),
            consignee = request.consignee.trimToNull(),
            currencyCode = request.currencyCode.trimToNull(),
            operationAmount = request.operationAmount,
            lastOperationDate = request.lastOperationDate,
            feaOfficerFullName = request.feaOfficerFullName.trimToNull(),
            decisionNote = request.decisionNote.trimToNull(),
            status = TransferComplianceStatus.NEW,
        )

        val saved = repository.save(entity)
        val files = bindAndLoadFiles(request.fileIds, requireNotNull(saved.id))
        return saved.toResponse(files)
    }

    private fun triggerTifSearch(entity: TransferComplianceCheck) {
        val command = TifSearchCommand(
            transferCheckId = requireNotNull(entity.id),
            externalId = entity.externalId,
            checkNumber = entity.checkNumber,
            clientName = entity.clientName,
            clientTin = entity.clientTin,
            foreignPartnerName = entity.foreignPartnerName,
            foreignPartnerTin = entity.foreignPartnerTin,
            username = currentUsername(),
        )

        val dispatch = {
            try {
                tifProcessor.process(command)
            } catch (ex: Exception) {
                logger.warn(ex) {
                    "Unable to dispatch background TIF search for externalId=${entity.externalId}. " +
                            "It can be retried later."
                }
            }
        }

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(object : TransactionSynchronization {
                override fun afterCommit() = dispatch()
            })
        } else {
            dispatch()
        }
    }

    @Transactional
    override fun update(id: UUID, request: TransferComplianceCheckUpdateRequest): TransferComplianceCheckResponse {
        val entity = repository.findById(id).orElseThrow {
            NotFoundException("Transfer compliance check not found for id=$id")
        }

        if (entity.status != TransferComplianceStatus.NEW) {
            throw BadRequestException("Only records in NEW status can be updated. Current status: ${entity.status}")
        }

        request.branchCode.trimToNull()?.let { entity.branchCode = it }
        request.clientName.trimToNull()?.let { entity.clientName = it }
        request.contractTypeCode.trimToNull()?.let { entity.contractTypeCode = it }
        request.contractNumber.trimToNull()?.let { entity.contractNumber = it }
        request.clientTin?.let { entity.clientTin = it.trimToNull() }
        request.enterpriseRegistrationDate?.let { entity.enterpriseRegistrationDate = it }
        request.activityType?.let { entity.activityType = it.trimToNull() }
        request.contractDate?.let { entity.contractDate = it }
        request.contractIdn?.let { entity.contractIdn = it.trimToNull() }
        request.foreignPartnerName?.let { entity.foreignPartnerName = it.trimToNull() }
        request.foreignPartnerTin?.let { entity.foreignPartnerTin = it.trimToNull() }
        request.directorAndRelatedParties?.let { entity.directorAndRelatedParties = it.trimToNull() }
        request.foreignPartnerCountryCode?.let { entity.foreignPartnerCountryCode = it.trimToNull() }
        request.foreignPartnerBank?.let { entity.foreignPartnerBank = it.trimToNull() }
        request.bicSwift?.let { entity.bicSwift = it.trimToNull() }
        request.foreignPartnerBankCountryCode?.let { entity.foreignPartnerBankCountryCode = it.trimToNull() }
        request.goodsType?.let { entity.goodsType = it }
        request.goodsName?.let { entity.goodsName = it.trimToNull() }
        request.goodsTnvedCode?.let { entity.goodsTnvedCode = it.trimToNull() }
        request.goodsOriginCountryCode?.let { entity.goodsOriginCountryCode = it.trimToNull() }
        request.consignor?.let { entity.consignor = it.trimToNull() }
        request.consignee?.let { entity.consignee = it.trimToNull() }
        request.currencyCode?.let { entity.currencyCode = it.trimToNull() }
        request.operationAmount?.let { entity.operationAmount = it }
        request.lastOperationDate?.let { entity.lastOperationDate = it }
        request.feaOfficerFullName?.let { entity.feaOfficerFullName = it.trimToNull() }
        request.decisionNote?.let { entity.decisionNote = it.trimToNull() }

        val saved = repository.save(entity)
        val entityId = requireNotNull(saved.id)
        attachmentService.syncEntityAttachments(entityId, ENTITY_TYPE, request.fileIds)
        val files = attachmentService.getByEntity(entityId, ENTITY_TYPE)
        return saved.toResponse(files)
    }

    @Transactional
    override fun updateStatusById(
        id: UUID,
        request: TransferComplianceCheckStatusUpdateRequest,
    ): TransferComplianceCheckResponse {
        val newStatus = requireNotNull(request.status) { "Property 'status' must be provided." }
        val entity = repository.findById(id).orElseThrow {
            NotFoundException("Transfer compliance check not found for id=$id")
        }

        if (newStatus != TransferComplianceStatus.SENT || entity.status !in SEND_ALLOWED_FROM) {
            throw BadRequestException(
                "This endpoint only allows transition to SENT from NEW or CHECK_ERROR. " +
                        "Current status: ${entity.status}, requested: $newStatus"
            )
        }

        entity.status = newStatus
        val saved = repository.save(entity)
        triggerTifSearch(saved)
        val files = attachmentService.getByEntity(requireNotNull(saved.id), ENTITY_TYPE)
        return saved.toResponse(files)
    }

    @Transactional
    override fun updateStatus(
        externalId: UUID,
        request: TransferComplianceCheckStatusUpdateRequest,
    ): TransferComplianceCheckResponse {
        val newStatus = requireNotNull(request.status) { "Property 'status' must be provided." }
        val entity = repository.findByExternalId(externalId)
            ?: throw NotFoundException("Transfer compliance check not found for externalId=$externalId")

        if (newStatus != TransferComplianceStatus.APPROVED && newStatus != TransferComplianceStatus.REJECTED) {
            throw BadRequestException(
                "This endpoint only allows APPROVED or REJECTED. Requested: $newStatus"
            )
        }

        if (!TransferComplianceStatus.canTransition(entity.status, newStatus)) {
            throw BadRequestException(
                "Invalid status transition from ${entity.status} to $newStatus. " +
                        "Record must be in CHECKED status to approve or reject."
            )
        }

        if (newStatus == TransferComplianceStatus.REJECTED) {
            entity.description = request.rejectReason.trimToNull()
                ?: throw BadRequestException("Property 'rejectReason' must be provided when rejecting.")
        }

        entity.status = newStatus
        entity.checkedBy = currentUsername()
        entity.checkedDate = LocalDateTime.now()

        val saved = repository.save(entity)
        val files = attachmentService.getByEntity(requireNotNull(saved.id), ENTITY_TYPE)
        return saved.toResponse(files)
    }

    private fun bindAndLoadFiles(fileIds: List<UUID>?, entityId: UUID): List<FileDetailDTO> {
        fileIds?.takeIf { it.isNotEmpty() }?.let {
            attachmentService.bindToEntity(it, entityId, ENTITY_TYPE)
        }
        return attachmentService.getByEntity(entityId, ENTITY_TYPE)
    }

    private fun nextCheckNumber(): String = "$CHECK_NUMBER_PREFIX${repository.nextCheckNumberSequence()}"

    private fun buildSpecification(
        keyword: String?,
        params: TransferComplianceCheckListFilter,
        allowedStatuses: Collection<TransferComplianceStatus>?,
    ): Specification<TransferComplianceCheck> =
        Specification { root, _, criteriaBuilder ->
            val predicates = mutableListOf<Predicate>()

            allowedStatuses?.takeIf { it.isNotEmpty() }?.let { statuses ->
                predicates += root.get<TransferComplianceStatus>(STATUS_FIELD).`in`(statuses)
            }
            params.status?.let { status ->
                predicates += criteriaBuilder.equal(root.get<TransferComplianceStatus>(STATUS_FIELD), status)
            }
            params.checkNumber.trimToNull()?.let { value ->
                predicates += criteriaBuilder.equal(root.get<String>(CHECK_NUMBER_FIELD), value)
            }
            params.externalId?.let { value ->
                predicates += criteriaBuilder.equal(root.get<UUID>(EXTERNAL_ID_FIELD), value)
            }
            params.branchCode.trimToNull()?.let { value ->
                predicates += criteriaBuilder.equal(root.get<String>(BRANCH_CODE_FIELD), value)
            }
            params.contractTypeCode.trimToNull()?.let { value ->
                predicates += criteriaBuilder.equal(root.get<String>(CONTRACT_TYPE_CODE_FIELD), value)
            }
            params.contractNumber.trimToNull()?.let { value ->
                predicates += criteriaBuilder.equal(root.get<String>(CONTRACT_NUMBER_FIELD), value)
            }
            params.contractIdn.trimToNull()?.let { value ->
                predicates += criteriaBuilder.equal(root.get<String>(CONTRACT_IDN_FIELD), value)
            }
            params.clientTin.trimToNull()?.let { value ->
                predicates += criteriaBuilder.equal(root.get<String>(CLIENT_TIN_FIELD), value)
            }
            params.clientName.trimToNull()?.let { value ->
                predicates += criteriaBuilder.like(
                    criteriaBuilder.lower(root.get(CLIENT_NAME_FIELD)),
                    value.lowercase(Locale.ROOT).containsPattern(),
                    LIKE_ESCAPE,
                )
            }
            params.foreignPartnerName.trimToNull()?.let { value ->
                predicates += criteriaBuilder.like(
                    criteriaBuilder.lower(root.get(FOREIGN_PARTNER_NAME_FIELD)),
                    value.lowercase(Locale.ROOT).containsPattern(),
                    LIKE_ESCAPE,
                )
            }
            params.foreignPartnerTin.trimToNull()?.let { value ->
                predicates += criteriaBuilder.equal(root.get<String>(FOREIGN_PARTNER_TIN_FIELD), value)
            }
            params.currencyCode.trimToNull()?.let { value ->
                predicates += criteriaBuilder.equal(root.get<String>(CURRENCY_CODE_FIELD), value)
            }
            params.goodsType?.let { value ->
                predicates += criteriaBuilder.equal(root.get<GoodsType>(GOODS_TYPE_FIELD), value)
            }
            params.checkedBy.trimToNull()?.let { value ->
                predicates += criteriaBuilder.equal(root.get<String>(CHECKED_BY_FIELD), value)
            }
            params.startDate?.let { start ->
                val startLdt = LocalDateTime.ofInstant(start, java.time.ZoneOffset.UTC)
                predicates += criteriaBuilder.greaterThanOrEqualTo(root.get(CREATED_DATE_FIELD), startLdt)
            }
            params.endDate?.let { end ->
                val endLdt = LocalDateTime.ofInstant(end, java.time.ZoneOffset.UTC)
                predicates += criteriaBuilder.lessThanOrEqualTo(root.get(CREATED_DATE_FIELD), endLdt)
            }
            keyword.trimToNull()?.let { value ->
                val pattern = value.lowercase(Locale.ROOT).containsPattern()
                predicates += criteriaBuilder.or(
                    *KEYWORD_FIELDS.map { field ->
                        criteriaBuilder.like(criteriaBuilder.lower(root.get(field)), pattern, LIKE_ESCAPE)
                    }.toTypedArray()
                )
            }

            if (predicates.isEmpty()) null else criteriaBuilder.and(*predicates.toTypedArray())
        }

    private fun BaseFilter.Pagination.toPageable(): Pageable {
        val boundedPage = page.coerceAtLeast(0)
        val boundedSize = size.coerceIn(1, MAX_PAGE_SIZE)
        val sortField = SORT_FIELDS[orderBy?.trim()?.lowercase(Locale.ROOT)] ?: DEFAULT_SORT_FIELD
        val direction = if (sortOrder?.equals(SORT_ASC, ignoreCase = true) == true) {
            Sort.Direction.ASC
        } else {
            Sort.Direction.DESC
        }

        return PageRequest.of(boundedPage, boundedSize, Sort.by(direction, sortField))
    }

    private fun currentUsername(): String? {
        val request = (RequestContextHolder.getRequestAttributes() as? ServletRequestAttributes)?.request
            ?: return null

        return request.headerNames
            ?.toList()
            ?.firstOrNull { headerName -> headerName.equals(USERNAME_HEADER, ignoreCase = true) }
            ?.let(request::getHeader)
            ?.trim()
            ?.takeIf(String::isNotBlank)
    }

    private fun String?.trimToNull(): String? = this?.trim()?.takeIf(String::isNotBlank)

    private fun String.containsPattern(): String =
        "%${replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_")}%"

    companion object {
        private val logger = KotlinLogging.logger {}
        private val ENTITY_TYPE = AttachmentEntityType.TRANSFER_COMPLIANCE_CHECK
        private const val CHECK_NUMBER_PREFIX = "TR_COM-"
        private const val MAX_PAGE_SIZE = 100
        private const val SORT_ASC = "asc"
        private const val LIKE_ESCAPE = '\\'
        private const val USERNAME_HEADER = "username"

        private const val ID_FIELD = "id"
        private const val STATUS_FIELD = "status"
        private const val CHECK_NUMBER_FIELD = "checkNumber"
        private const val EXTERNAL_ID_FIELD = "externalId"
        private const val BRANCH_CODE_FIELD = "branchCode"
        private const val CONTRACT_TYPE_CODE_FIELD = "contractTypeCode"
        private const val CONTRACT_NUMBER_FIELD = "contractNumber"
        private const val CONTRACT_IDN_FIELD = "contractIdn"
        private const val CLIENT_NAME_FIELD = "clientName"
        private const val CLIENT_TIN_FIELD = "clientTin"
        private const val FOREIGN_PARTNER_NAME_FIELD = "foreignPartnerName"
        private const val FOREIGN_PARTNER_TIN_FIELD = "foreignPartnerTin"
        private const val CURRENCY_CODE_FIELD = "currencyCode"
        private const val GOODS_TYPE_FIELD = "goodsType"
        private const val CHECKED_BY_FIELD = "checkedBy"
        private const val CHECKED_DATE_FIELD = "checkedDate"
        private const val CREATED_DATE_FIELD = "createdDate"
        private const val DEFAULT_SORT_FIELD = CREATED_DATE_FIELD

        private val EXTERNAL_VISIBLE_STATUSES = listOf(
            TransferComplianceStatus.CHECKED, TransferComplianceStatus.APPROVED, TransferComplianceStatus.REJECTED
        )

        private val SEND_ALLOWED_FROM = setOf(
            TransferComplianceStatus.NEW,
            TransferComplianceStatus.CHECK_ERROR,
        )

        private val RESPONSE_EXCLUDED_STATUSES = setOf(
            TransferComplianceStatus.NEW,
            TransferComplianceStatus.CHECK_ERROR,
        )

        private val KEYWORD_FIELDS = listOf(
            CHECK_NUMBER_FIELD,
            CONTRACT_NUMBER_FIELD,
            CLIENT_NAME_FIELD,
            CLIENT_TIN_FIELD,
            FOREIGN_PARTNER_NAME_FIELD,
        )

        private val SORT_FIELDS = mapOf(
            ID_FIELD to ID_FIELD,
            STATUS_FIELD to STATUS_FIELD,
            CHECK_NUMBER_FIELD.lowercase(Locale.ROOT) to CHECK_NUMBER_FIELD,
            CONTRACT_NUMBER_FIELD.lowercase(Locale.ROOT) to CONTRACT_NUMBER_FIELD,
            CLIENT_NAME_FIELD.lowercase(Locale.ROOT) to CLIENT_NAME_FIELD,
            BRANCH_CODE_FIELD.lowercase(Locale.ROOT) to BRANCH_CODE_FIELD,
            CHECKED_BY_FIELD.lowercase(Locale.ROOT) to CHECKED_BY_FIELD,
            CHECKED_DATE_FIELD.lowercase(Locale.ROOT) to CHECKED_DATE_FIELD,
            CREATED_DATE_FIELD.lowercase(Locale.ROOT) to CREATED_DATE_FIELD,
        )
    }
}
