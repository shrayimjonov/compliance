package uz.davrbank.compliance.service.impl

import jakarta.persistence.criteria.Predicate
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.jpa.domain.Specification
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uz.davrbank.compliance.domain.*
import uz.davrbank.compliance.dto.request.filter.BaseFilter
import uz.davrbank.compliance.dto.request.filter.LocalComplianceListFilter
import uz.davrbank.compliance.dto.request.local.*
import uz.davrbank.compliance.dto.response.MessageResponse
import uz.davrbank.compliance.dto.response.local.ComplianceAdminResponse
import uz.davrbank.compliance.dto.response.local.toAdminResponse
import uz.davrbank.compliance.enums.ComplianceType
import uz.davrbank.compliance.enums.DataType
import uz.davrbank.compliance.enums.Type
import uz.davrbank.compliance.exception.BadRequestException
import uz.davrbank.compliance.exception.NotFoundException
import uz.davrbank.compliance.repository.*
import uz.davrbank.compliance.service.ComplianceSearchIndexService
import uz.davrbank.compliance.service.LocalComplianceService
import java.util.*

@Service
class LocalComplianceServiceImpl(
    private val complianceRepository: ComplianceRepository,
    private val entityAddressRepository: EntityAddressRepository,
    private val entityPositionRepository: EntityPositionRepository,
    private val entityRemarkRepository: EntityRemarkRepository,
    private val entitySourceItemRepository: EntitySourceItemRepository,
    private val complianceSearchIndexService: ComplianceSearchIndexService,
) : LocalComplianceService {

    @Transactional
    override fun create(request: ComplianceAdminRequest): ComplianceAdminResponse {
        request.dataType = DataType.BANK
        request.complianceType = ComplianceType.ENTITY
        val compliance = complianceRepository.save(request.toEntity())

        syncAddresses(compliance, request.addressRequest)
        syncPositions(compliance, request.position)
        syncSourceItems(compliance, request.sourceItemRequest)
        syncRemarks(compliance, request.remark)
        complianceSearchIndexService.save(compliance)

        return compliance.toLocalAdminResponse()
    }

    @Transactional(readOnly = true)
    override fun getList(filter: BaseFilter<LocalComplianceListFilter>): Page<ComplianceAdminResponse> {
        val params = filter.params ?: LocalComplianceListFilter()
        val pagination = requireNotNull(filter.pagination) { "Property 'pagination' must be provided." }
        val pageable = pagination.toPageable(MAX_PAGE_SIZE)

        val page: Page<Compliance>? =
            complianceRepository.findAll(buildListSpecification(filter.keyword, params), pageable)

        return (page ?: Page.empty(pageable)).map { it.toLocalAdminResponse() }
    }

    @Transactional(readOnly = true)
    override fun get(id: String): ComplianceAdminResponse =
        findCompliance(id).toLocalAdminResponse()

    @Transactional
    override fun update(id: String, request: ComplianceAdminRequest): ComplianceAdminResponse {
        val compliance = findCompliance(id)
        val resolvedName = request.resolveName()

        compliance.apply {
            type = resolvedName.type
            gender = request.gender?.trimToNull() ?: DEFAULT_GENDER
            name = resolvedName.fullName
            this.firstName = resolvedName.firstName
            middleName = request.middleName?.trimToNull()
            this.lastName = resolvedName.lastName
            this.fullName = resolvedName.fullName
            title = request.title
            isDeceased = request.isDeceased
            deceasedDate = request.deceasedDate
            complianceType = required("complianceType", request.complianceType)
            dataType = required("dataType", request.dataType)
            tin = request.tin
            pinfl = request.pinfl
            lastUpdated = Date()
        }

        syncAddresses(compliance, request.addressRequest)
        syncPositions(compliance, request.position)
        syncSourceItems(compliance, request.sourceItemRequest)
        syncRemarks(compliance, request.remark)
        complianceSearchIndexService.save(compliance)

        return complianceRepository.save(compliance).toLocalAdminResponse()
    }

    @Transactional
    override fun delete(id: String): MessageResponse {
        val compliance = findCompliance(id)

        deleteLocalChildren(compliance.id)
        complianceRepository.delete(compliance)
        complianceSearchIndexService.delete(compliance.id)

        return MessageResponse("Compliance deleted successfully")
    }

    private fun ComplianceAdminRequest.toEntity(): Compliance {
        val resolvedName = resolveName()

        return Compliance(
            id = UUID.randomUUID().toString(),
            type = resolvedName.type,
            gender = gender?.trimToNull() ?: DEFAULT_GENDER,
            name = resolvedName.fullName,
            firstName = resolvedName.firstName,
            middleName = middleName?.trimToNull(),
            lastName = resolvedName.lastName,
            fullName = resolvedName.fullName,
            prefix = null,
            suffix = null,
            title = title,
            isDeceased = isDeceased,
            deceasedDate = deceasedDate,
            entityId = null,
            lookupId = null,
            lastUpdated = Date(),
            complianceType = required("complianceType", complianceType),
            dataType = required("dataType", dataType),
            tin = tin,
            pinfl = pinfl,
        )
    }

    private fun syncAddresses(compliance: Compliance, requests: List<AddressRequest>) {
        val existingAddresses = entityAddressRepository.findAllByComplianceId(compliance.id)
            .associateBy { it.id }
            .toMutableMap()
        val addressesToSave = requests.map { request ->
            val existingAddress = request.id.normalizedId()?.let(existingAddresses::remove)

            existingAddress?.apply {
                addressTypeDesc = request.addressTypeDesc
                city = request.city
                stateProvinceRegion = request.stateProvinceRegion
                country = request.country
                isoStandard = request.isoStandard
                postalCode = request.postalCode
                address1 = request.address1
                address2 = request.address2
                lastUpdated = Date()
            } ?: EntityAddress(
                id = UUID.randomUUID().toString(),
                addressTypeDesc = request.addressTypeDesc,
                city = request.city,
                stateProvinceRegion = request.stateProvinceRegion,
                country = request.country,
                isoStandard = request.isoStandard,
                lastUpdated = Date(),
                postalCode = request.postalCode,
                address1 = request.address1,
                address2 = request.address2,
                compliance = compliance,
            )
        }

        if (addressesToSave.isNotEmpty()) {
            entityAddressRepository.saveAll(addressesToSave)
        }
        if (existingAddresses.isNotEmpty()) {
            entityAddressRepository.deleteAll(existingAddresses.values)
        }
    }

    private fun syncPositions(compliance: Compliance, requests: List<PositionRequest>) {
        val existingPositions = entityPositionRepository.findAllByComplianceId(compliance.id)
            .associateBy { it.id }
            .toMutableMap()
        val positionsToSave = requests.map { request ->
            val existingPosition = request.id.normalizedId()?.let(existingPositions::remove)

            existingPosition?.apply {
                position = request.position
                lastUpdated = Date()
            } ?: EntityPosition(
                id = UUID.randomUUID().toString(),
                position = request.position,
                lastUpdated = Date(),
                compliance = compliance,
            )
        }

        if (positionsToSave.isNotEmpty()) {
            entityPositionRepository.saveAll(positionsToSave)
        }
        if (existingPositions.isNotEmpty()) {
            entityPositionRepository.deleteAll(existingPositions.values)
        }
    }

    private fun syncSourceItems(compliance: Compliance, requests: List<SourceItemRequest>) {
        val existingSourceItems = entitySourceItemRepository.findAllByComplianceId(compliance.id)
            .associateBy { it.id }
            .toMutableMap()
        val sourceItemsToSave = requests.map { request ->
            val existingSourceItem = request.id.normalizedId()?.let(existingSourceItems::remove)

            existingSourceItem?.apply {
                sourceURI = request.sourceURI
                lastUpdated = Date()
            } ?: EntitySourceItem(
                id = UUID.randomUUID().toString(),
                sourceURI = request.sourceURI,
                lastUpdated = Date(),
                compliance = compliance,
            )
        }

        if (sourceItemsToSave.isNotEmpty()) {
            entitySourceItemRepository.saveAll(sourceItemsToSave)
        }
        if (existingSourceItems.isNotEmpty()) {
            entitySourceItemRepository.deleteAll(existingSourceItems.values)
        }
    }

    private fun syncRemarks(compliance: Compliance, requests: List<RemarkRequest>) {
        val existingRemarks = entityRemarkRepository.findAllByComplianceId(compliance.id)
            .associateBy { it.id }
            .toMutableMap()
        val remarksToSave = requests.map { request ->
            val existingRemark = request.id.normalizedId()?.let(existingRemarks::remove)

            existingRemark?.apply {
                remark = request.remark
                lastUpdated = Date()
            } ?: EntityRemark(
                id = UUID.randomUUID().toString(),
                remark = request.remark,
                lastUpdated = Date(),
                compliance = compliance,
            )
        }

        if (remarksToSave.isNotEmpty()) {
            entityRemarkRepository.saveAll(remarksToSave)
        }
        if (existingRemarks.isNotEmpty()) {
            entityRemarkRepository.deleteAll(existingRemarks.values)
        }
    }

    private fun Compliance.toLocalAdminResponse(): ComplianceAdminResponse = toAdminResponse(
        sourceItems = emptyIfNull(entitySourceItemRepository.findAllByComplianceId(id))
            .sortedBy { it.id }
            .map { it.toAdminResponse() },
        addresses = emptyIfNull(entityAddressRepository.findAllByComplianceId(id))
            .sortedBy { it.id }
            .map { it.toAdminResponse() },
        remarks = emptyIfNull(entityRemarkRepository.findAllByComplianceId(id))
            .sortedBy { it.id }
            .map { it.toAdminResponse() },
        positions = emptyIfNull(entityPositionRepository.findAllByComplianceId(id))
            .sortedBy { it.id }
            .map { it.toAdminResponse() },
    )

    private fun <T> emptyIfNull(items: List<T>?): List<T> = items ?: emptyList()

    private fun findCompliance(id: String): Compliance =
        complianceRepository.findByIdOrNull(id)
            ?: throw NotFoundException("Compliance entity not found for id=$id")

    private fun deleteLocalChildren(complianceId: String) {
        entityAddressRepository.deleteAll(entityAddressRepository.findAllByComplianceId(complianceId))
        entityPositionRepository.deleteAll(entityPositionRepository.findAllByComplianceId(complianceId))
        entityRemarkRepository.deleteAll(entityRemarkRepository.findAllByComplianceId(complianceId))
        entitySourceItemRepository.deleteAll(entitySourceItemRepository.findAllByComplianceId(complianceId))
    }

    private fun buildListSpecification(
        keyword: String?,
        params: LocalComplianceListFilter,
    ): Specification<Compliance> = Specification { root, _, criteriaBuilder ->
        val predicates = mutableListOf<Predicate>()
        params.dataType = DataType.BANK

        params.type?.let {
            predicates += criteriaBuilder.equal(root.get<uz.davrbank.compliance.enums.Type>(TYPE_FIELD), it)
        }
        params.complianceType?.let {
            predicates += criteriaBuilder.equal(
                root.get<uz.davrbank.compliance.enums.ComplianceType>(
                    COMPLIANCE_TYPE_FIELD
                ), it
            )
        }
        params.dataType?.let {
            predicates += criteriaBuilder.equal(root.get<DataType>(DATA_TYPE_FIELD), it)
        }
        params.tin.trimToNull()?.let {
            predicates += criteriaBuilder.equal(root.get<String>(TIN_FIELD), it)
        }
        params.pinfl.trimToNull()?.let {
            predicates += criteriaBuilder.equal(root.get<String>(PINFL_FIELD), it)
        }
        keyword.trimToNull()?.let { normalizedKeyword ->
            predicates += criteriaBuilder.or(
                *LOCAL_KEYWORD_FIELDS.map { fieldName ->
                    criteriaBuilder.like(
                        criteriaBuilder.lower(root.get(fieldName)),
                        normalizedKeyword.containsPattern()
                    )
                }.toTypedArray()
            )
        }

        criteriaBuilder.and(*predicates.toTypedArray())
    }

    private fun ComplianceAdminRequest.resolveName(): ResolvedName {
        val requestType = required("type", type)
        val firstName = firstname.trimToNull()
        val lastName = lastname.orEmpty().trim()

        if (requestType == Type.Organization) {
            val organizationName = title.trimToNull()
                ?: buildFullName(firstName, lastName).trimToNull()
                ?: throw BadRequestException("Property 'title' must be provided for Organization.")

            return ResolvedName(
                type = requestType,
                firstName = firstName,
                lastName = lastName,
                fullName = organizationName,
            )
        }

        val requiredFirstName = firstName ?: throw BadRequestException("Property 'firstname' must be provided.")

        return ResolvedName(
            type = requestType,
            firstName = requiredFirstName,
            lastName = lastName,
            fullName = buildFullName(requiredFirstName, lastName),
        )
    }

    private fun buildFullName(firstName: String?, lastName: String?): String =
        listOf(firstName, lastName)
            .mapNotNull { it.trimToNull() }
            .joinToString(" ")

    private fun <T : Any> required(fieldName: String, value: T?): T =
        value ?: throw BadRequestException("Property '$fieldName' must be provided.")

    private fun String?.normalizedId(): String? = this?.trim()?.takeIf { it.isNotBlank() }

    private fun String?.trimToNull(): String? = this?.trim()?.takeIf { it.isNotBlank() }

    private fun String.containsPattern(): String = "%${lowercase()}%"

    companion object {
        private const val DEFAULT_GENDER = "Not applicable"
        private val logger = LoggerFactory.getLogger(LocalComplianceServiceImpl::class.java)
        private const val MAX_PAGE_SIZE = 1000
        private const val TYPE_FIELD = "type"
        private const val NAME_FIELD = "name"
        private const val FIRST_NAME_FIELD = "firstName"
        private const val MIDDLE_NAME_FIELD = "middleName"
        private const val LAST_NAME_FIELD = "lastName"
        private const val FULL_NAME_FIELD = "fullName"
        private const val COMPLIANCE_TYPE_FIELD = "complianceType"
        private const val DATA_TYPE_FIELD = "dataType"
        private const val TIN_FIELD = "tin"
        private const val PINFL_FIELD = "pinfl"
        private val LOCAL_KEYWORD_FIELDS = listOf(
            NAME_FIELD,
            FIRST_NAME_FIELD,
            MIDDLE_NAME_FIELD,
            LAST_NAME_FIELD,
            FULL_NAME_FIELD,
            TIN_FIELD,
            PINFL_FIELD,
        )
    }

    private data class ResolvedName(
        val type: Type,
        val firstName: String?,
        val lastName: String,
        val fullName: String,
    )
}
