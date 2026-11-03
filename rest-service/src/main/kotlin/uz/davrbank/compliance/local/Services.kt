package uz.davrbank.compliance.local


import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uz.davrbank.compliance.DataType
import uz.davrbank.compliance.GeneralApiException
import uz.davrbank.compliance.backup.*
import uz.davrbank.compliance.local.ComplianceAdminRequest.Companion.toEntity
import uz.davrbank.compliance.dropEsChars
import uz.davrbank.compliance.elasticsearch.*
import java.util.*

interface LocalComplianceService {
    fun addData(request: ComplianceAdminRequest)
    fun getAll(pageable: Pageable): Page<ComplianceAdminResponse>
    fun update(id: String, request: ComplianceAdminRequest)
    fun getOne(id: String): ComplianceAdminResponse
    fun delete(id: Long)
}

@Service
class LocalComplianceServiceImpl(
    private val esService: ESService,

    private val complianceRepository: ComplianceEntityPRepository,
    private val elasticsearchRepository: ComplianceDataRepository,
    private val addressRepository: EntityAddressRepository,
    private val positionRepository: EntityPositionRepository,
    private val remarkRepository: EntityRemarkRepository,
    private val sourceItemRepository: EntitySourceItemRepository
) : LocalComplianceService {



    override fun addData(request: ComplianceAdminRequest) {
        val compliance = complianceRepository.save(request.toEntity())
        createOrUpdatePositions(compliance, request.position)
        createOrUpdateAddresses(compliance, request.addressRequest)
        createOrUpdateSourceItems(compliance, request.sourceItemRequest)
        createOrUpdateRemarks(compliance, request.remark)

        esService.save(
            NewComplianceData(
                compliance.firstName ?: "",
                compliance.lastName,
                compliance.fullName,
                UUID.randomUUID().toString(),
                compliance.type.name,
                compliance.complianceType,
                compliance.id,
                compliance.dataType
            )
        )

    }

    override fun getAll(pageable: Pageable): Page<ComplianceAdminResponse> {
        return complianceRepository.findAllByDataType(DataType.BANK, pageable).map { compliance ->
            val addressAdminResponses =
                addressRepository.findAllByCompliance(compliance).map { address ->
                    AddressAdminResponse.toResponse(address)
                }
            val positionAdminResponses =
                positionRepository.findAllByCompliance(compliance).map { position ->
                    PositionAdminResponse.toResponse(position)
                }

            val remarkAdminResponses = remarkRepository.findAllByCompliance(compliance).map { remark ->
                RemarkAdminResponse.toResponse(remark)
            }

            val sourceItemAdminResponses =
                sourceItemRepository.findAllByCompliance(compliance).map { sourceItem ->
                    SourceItemAdminResponse.toResponse(sourceItem)
                }

            ComplianceAdminResponse.toResponse(
                compliance,
                sourceItemAdminResponses,
                addressAdminResponses,
                remarkAdminResponses,
                positionAdminResponses
            )
        }

    }

    @Transactional
    override fun update(id: String, request: ComplianceAdminRequest) {
        val compliance = complianceRepository.findByIdOrNull(id)
            ?: throw GeneralApiException("Compliance not found")


        compliance.apply {
            type = request.type
            gender = request.gender ?: "Not applicable"
            name = request.firstname
            firstName = request.firstname
            lastName = request.lastname ?: ""
            middleName = request.middleName
            title = request.title
            isDeceased = request.isDeceased
            deceasedDate = request.deceasedDate
            tin = request.tin
            pinfl = request.pinfl
        }


        createOrUpdateAddresses(compliance, request.addressRequest)
        createOrUpdatePositions(compliance, request.position)
        createOrUpdateSourceItems(compliance, request.sourceItemRequest)
        createOrUpdateRemarks(compliance, request.remark)


        updateElasticsearch(compliance, request)


        complianceRepository.save(compliance)
    }

    private fun createOrUpdateAddresses(compliance: Compliance, addressRequests: List<AddressRequest>) {
        val existingAddresses = addressRepository.findAllByCompliance(compliance)
            .associateBy { it.id }
            .toMutableMap()
        val addressesToSave = mutableListOf<EntityAddress>()

        addressRequests.forEach { request ->
            val existingAddress = existingAddresses.remove(request.id)
            if (existingAddress != null) {
                existingAddress.apply {
                    addressTypeDesc = request.addressTypeDesc
                    city = request.city
                    stateProvinceRegion = request.stateProvinceRegion
                    country = request.country
                    isoStandard = request.isoStandard
                    postalCode = request.postalCode
                    address1 = request.address1
                    address2 = request.address2
                }
                addressesToSave.add(existingAddress)
            } else {
                val newAddress = EntityAddress(
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
                    compliance = compliance
                )
                addressesToSave.add(newAddress)
            }
        }


        if (addressesToSave.isNotEmpty()) {
            addressRepository.saveAll(addressesToSave)
        }


        if (existingAddresses.isNotEmpty()) {
            addressRepository.deleteAll(existingAddresses.values)
        }
    }

    private fun createOrUpdatePositions(compliance: Compliance, positionRequests: List<PositionRequest>) {
        val existingPositions = positionRepository.findAllByCompliance(compliance)
            .associateBy { it.id }
            .toMutableMap()
        val positionsToSave = mutableListOf<EntityPosition>()

        positionRequests.forEach { request ->
            val existingPosition = existingPositions.remove(request.id)
            if (existingPosition != null) {
                existingPosition.position = request.position
                positionsToSave.add(existingPosition)
            } else {

                val newPosition = EntityPosition(
                    id = UUID.randomUUID().toString(),
                    position = request.position,
                    lastUpdated = Date(),
                    compliance = compliance
                )
                positionsToSave.add(newPosition)
            }
        }


        if (positionsToSave.isNotEmpty()) {
            positionRepository.saveAll(positionsToSave)
        }


        if (existingPositions.isNotEmpty()) {
            positionRepository.deleteAll(existingPositions.values)
        }
    }

    private fun createOrUpdateSourceItems(compliance: Compliance, sourceItemRequests: List<SourceItemRequest>) {
        val existingSourceItems = sourceItemRepository.findAllByCompliance(compliance)
            .associateBy { it.id }
            .toMutableMap()
        val sourceItemsToSave = mutableListOf<EntitySourceItem>()

        sourceItemRequests.forEach { request ->
            val existingSourceItem = existingSourceItems.remove(request.id)
            if (existingSourceItem != null) {
                existingSourceItem.sourceURI = request.sourceURI
                sourceItemsToSave.add(existingSourceItem)
            } else {
                val newSourceItem = EntitySourceItem(
                    id = UUID.randomUUID().toString(),
                    sourceURI = request.sourceURI,
                    lastUpdated = Date(),
                    compliance = compliance
                )
                sourceItemsToSave.add(newSourceItem)
            }
        }


        if (sourceItemsToSave.isNotEmpty()) {
            sourceItemRepository.saveAll(sourceItemsToSave)
        }


        if (existingSourceItems.isNotEmpty()) {
            sourceItemRepository.deleteAll(existingSourceItems.values)
        }
    }

    private fun createOrUpdateRemarks(compliance: Compliance, remarkRequests: List<RemarkRequest>) {
        val existingRemarks = remarkRepository.findAllByCompliance(compliance)
            .associateBy { it.id }
            .toMutableMap()
        val remarksToSave = mutableListOf<EntityRemark>()

        remarkRequests.forEach { request ->
            val existingRemark = existingRemarks.remove(request.id)
            if (existingRemark != null) {
                existingRemark.remark = request.remark
                remarksToSave.add(existingRemark)
            } else {
                val newRemark = EntityRemark(
                    id = UUID.randomUUID().toString(),
                    remark = request.remark,
                    lastUpdated = Date(),
                    compliance = compliance
                )
                remarksToSave.add(newRemark)
            }
        }


        if (remarksToSave.isNotEmpty()) {
            remarkRepository.saveAll(remarksToSave)
        }


        if (existingRemarks.isNotEmpty()) {
            remarkRepository.deleteAll(existingRemarks.values)
        }
    }

    private fun updateElasticsearch(compliance: Compliance, request: ComplianceAdminRequest) {
        elasticsearchRepository.findByIdOrNull(compliance.id)?.let { es ->
            es.apply {
                type = request.type.name
                complianceType = request.complianceType
                fullName = request.firstname + (request.lastname ?: "")
                dataType = request.dataType
                lastname = request.lastname?.dropEsChars() ?: ""
                firstname = request.firstname.dropEsChars()
            }
            elasticsearchRepository.save(es)
        }

    }


    override fun getOne(id: String): ComplianceAdminResponse {
        return complianceRepository.findByIdOrNull(id)?.let { compliance ->
            val addressAdminResponses =
                addressRepository.findAllByCompliance(compliance).map { address ->
                    AddressAdminResponse.toResponse(address)
                }
            val positionAdminResponses =
                positionRepository.findAllByCompliance(compliance).map { position ->
                    PositionAdminResponse.toResponse(position)
                }

            val remarkAdminResponses = remarkRepository.findAllByCompliance(compliance).map { remark ->
                RemarkAdminResponse.toResponse(remark)
            }

            val sourceItemAdminResponses =
                sourceItemRepository.findAllByCompliance(compliance).map { sourceItem ->
                    SourceItemAdminResponse.toResponse(sourceItem)
                }

            ComplianceAdminResponse.toResponse(
                compliance,
                sourceItemAdminResponses,
                addressAdminResponses,
                remarkAdminResponses,
                positionAdminResponses
            )
        } ?: throw GeneralApiException("Compliance not found")
    }

    override fun delete(id: Long) {
        TODO("Not yet implemented")
    }
}
