package uz.davrbank.compliance.risk

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import uz.davrbank.compliance.ComplianceType
import uz.davrbank.compliance.GeneralApiException
import uz.davrbank.compliance.backup.AdditionalSegmentRepository
import uz.davrbank.compliance.backup.Compliance
import uz.davrbank.compliance.backup.ComplianceEntityPRepository
import uz.davrbank.compliance.backup.ComplianceRelatedEntityDataRiskResponse
import uz.davrbank.compliance.backup.EntityAddressRepository
import uz.davrbank.compliance.backup.EntityAdverseMediaRepository
import uz.davrbank.compliance.backup.EntityAdverseMediaSubCategoryRepository
import uz.davrbank.compliance.backup.EntityAliasRepository
import uz.davrbank.compliance.backup.EntityCountryAssociationRepository
import uz.davrbank.compliance.backup.EntityDOBRepository
import uz.davrbank.compliance.backup.EntityEnforcementRepository
import uz.davrbank.compliance.backup.EntityEnforcementSubcategoryRepository
import uz.davrbank.compliance.backup.EntityIdentificationRepository
import uz.davrbank.compliance.backup.EntityPepRepository
import uz.davrbank.compliance.backup.EntityPepSubCategoryRepository
import uz.davrbank.compliance.backup.EntityPositionRepository
import uz.davrbank.compliance.backup.EntityRelationshipRepository
import uz.davrbank.compliance.backup.EntityRemarkRepository
import uz.davrbank.compliance.backup.EntitySOEDomainRepository
import uz.davrbank.compliance.backup.EntitySOESubcategoryRepository
import uz.davrbank.compliance.backup.EntitySanctionRepository
import uz.davrbank.compliance.backup.EntitySegmentRepository
import uz.davrbank.compliance.backup.EntitySoeRepository
import uz.davrbank.compliance.backup.EntitySourceItemRepository
import uz.davrbank.compliance.backup.FieldRepository
import uz.davrbank.compliance.backup.RecordRepository
import uz.davrbank.compliance.cache.ComplianceCacheService
import uz.davrbank.compliance.local.ComplianceSearchRequest
import uz.davrbank.compliance.dropEsChars
import uz.davrbank.compliance.elasticsearch.ESCheckService
import uz.davrbank.compliance.elasticsearch.ESHits
import uz.davrbank.compliance.elasticsearch.SearchTarget
import uz.davrbank.compliance.elasticsearch.toResponse
import java.time.LocalDate
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors

interface RiskService {
    fun checkComplianceMaxHit(request: ComplianceSearchRequest): ComplianceResponse
    fun checkData(request: ComplianceSearchRequest, pageable: Pageable): Any
    fun getIndividualRiskProfileV2(complianceEntityId: String): ComplianceDataRiskResponse
    fun getIndividualRiskProfileV3(complianceEntityId: String): ComplianceDataRiskResponse
    fun search(request: ComplianceSearchRequest, pageable: Pageable): Any
}


@Service
class RiskServiceImpl(
    private val complianceEntityPRepository: ComplianceEntityPRepository,
    private val relationshipRepository: EntityRelationshipRepository,
    private val positionRepository: EntityPositionRepository,
    private val addressRepository: EntityAddressRepository,
    private val remarkRepository: EntityRemarkRepository,
    private val sourceItemRepository: EntitySourceItemRepository,
    private val entitySegmentRepository: EntitySegmentRepository,
    private val entitySanctionRepository: EntitySanctionRepository,
    private val entityPepRepository: EntityPepRepository,
    private val entityEnforcementRepository: EntityEnforcementRepository,
    private val entityAdverseMediaRepository: EntityAdverseMediaRepository,
    private val entitySoeRepository: EntitySoeRepository,
    private val cacheService: ComplianceCacheService,
    private val esCheckService: ESCheckService,
    private val entityPepSubCategoryRepository: EntityPepSubCategoryRepository,
    private val entityEnforcementSubcategoryRepository: EntityEnforcementSubcategoryRepository,
    private val adverseMediaSubCategoryRepository: EntityAdverseMediaSubCategoryRepository,
    private val entitySOESubcategoryRepository: EntitySOESubcategoryRepository,
    private val entitySOEDomainRepository: EntitySOEDomainRepository,
    private val entityAliasRepository: EntityAliasRepository,
    private val entityCountryAssociationRepository: EntityCountryAssociationRepository,
    private val entityIdentificationRepository: EntityIdentificationRepository,
    private val additionalSegmentRepository: AdditionalSegmentRepository,
    private val recordRepository: RecordRepository,
    private val fieldRepository: FieldRepository,
    private val entityDOBRepository: EntityDOBRepository
) : RiskService {


    private val nullOrBlankStringField = "1q2w3e4r5t"

    override fun checkData(request: ComplianceSearchRequest, pageable: Pageable): Page<ESHits> {
        val hits = if (request.target == SearchTarget.INDIVIDUAL) {
            esCheckService.findIndividuals(
                request.firstname?.dropEsChars()?.lowercase() ?: nullOrBlankStringField,
                request.lastname?.dropEsChars()?.lowercase() ?: nullOrBlankStringField,
                pageable
            )
        } else {
            esCheckService.findOrganizations(
                request.name?.dropEsChars()?.lowercase() ?: nullOrBlankStringField,
                pageable
            )
        }

        return hits
    }


    override fun checkComplianceMaxHit(request: ComplianceSearchRequest): ComplianceResponse {
        val cacheFound = cacheService.findByRequest(request)
        if (cacheFound != null) {
            return cacheFound.toResponse()
        }
        val hits = if (request.target == SearchTarget.INDIVIDUAL) {
            esCheckService.findIndividualsInternal(
                request.firstname?.dropEsChars() ?: nullOrBlankStringField,
                request.lastname?.dropEsChars() ?: nullOrBlankStringField
            )
        } else {
            esCheckService.findOrganizationsInternal(request.name ?: nullOrBlankStringField)

        }
        cacheService.cache(request, hits)
        return hits.toResponse()
    }


    override fun getIndividualRiskProfileV2(complianceEntityId: String): ComplianceDataRiskResponse {
        val complianceEntity = complianceEntityPRepository.findByIdOrNull(complianceEntityId)
            ?: throw GeneralApiException("Compliance entity not found with id: $complianceEntityId")


        val relatedEntities = fetchRelatedEntities(complianceEntity)
        val aliases = fetchAliases(complianceEntity)
        val addresses = fetchAddresses(complianceEntity)
        val positions = fetchPositions(complianceEntity)
        val remarks = fetchRemarks(complianceEntity)
        val sourceItems = fetchSourceItems(complianceEntity)
        val riskSegments = buildRiskSegments(complianceEntity)
        val countryAssociations = fetchCountryAssociations(complianceEntity)
        val identifications = fetchIdentifications(complianceEntity)
        val additionalSegments = fetchAdditionalSegments(complianceEntity)

        val country =
            countryAssociations.filter { it.associationTypeDesc == "Citizenship" || it.associationTypeDesc == "Nationality" }
                .associateBy { it.isostandard }.values.toList()

        val birthDate = fetchDob(complianceEntity)
        complianceEntity.complianceType
        return ComplianceDataRiskResponse(
            fullName = complianceEntity.fullName,
            gender = complianceEntity.gender,
            isDeceased = complianceEntity.isDeceased,
            id = complianceEntity.id,
            complianceType = complianceEntity.complianceType,
            positions = positions,
            lastUpdated = complianceEntity.lastUpdated.time,
            aliases = aliases,
            addresses = addresses,
            remarks = remarks,
            sourceItems = sourceItems,
            riskSegments = riskSegments,
            relatedEntities = relatedEntities,
            countryAssociations = countryAssociations,
            identifications = identifications,
            additionalSegments = additionalSegments,
            country = country,
            birthDate = birthDate,
            type = complianceEntity.type
        )
    }





    private val executor = Executors.newFixedThreadPool(15)

    override fun getIndividualRiskProfileV3(complianceEntityId: String): ComplianceDataRiskResponse {
        val complianceEntity = complianceEntityPRepository.findByIdOrNull(complianceEntityId)
            ?: throw GeneralApiException("Compliance entity not found with id: $complianceEntityId")


        val relatedEntitiesFut = CompletableFuture.supplyAsync({ fetchRelatedEntities(complianceEntity) }, executor)
        val aliasesFut = CompletableFuture.supplyAsync({ fetchAliases(complianceEntity) }, executor)
        val addressesFut = CompletableFuture.supplyAsync({ fetchAddresses(complianceEntity) }, executor)
        val positionsFut = CompletableFuture.supplyAsync({ fetchPositions(complianceEntity) }, executor)
        val remarksFut = CompletableFuture.supplyAsync({ fetchRemarks(complianceEntity) }, executor)
        val sourceItemsFut = CompletableFuture.supplyAsync({ fetchSourceItems(complianceEntity) }, executor)
        val riskSegmentsFut = CompletableFuture.supplyAsync({ buildRiskSegments(complianceEntity) }, executor)
        val identificationsFut = CompletableFuture.supplyAsync({ fetchIdentifications(complianceEntity) }, executor)
        val additionalSegmentsFut = CompletableFuture.supplyAsync({ fetchAdditionalSegments(complianceEntity) }, executor)
        val birthDateFut = CompletableFuture.supplyAsync({ fetchDob(complianceEntity) }, executor)


        val countryAssociationsFut = CompletableFuture.supplyAsync({ fetchCountryAssociations(complianceEntity) }, executor)


        val countryFut = countryAssociationsFut.thenApply { list ->
            list.filter { it.associationTypeDesc == "Citizenship" || it.associationTypeDesc == "Nationality" }
                .associateBy { it.isostandard }.values.toList()
        }


        return ComplianceDataRiskResponse(
            fullName = complianceEntity.fullName,
            gender = complianceEntity.gender,
            isDeceased = complianceEntity.isDeceased,
            id = complianceEntity.id,
            complianceType = complianceEntity.complianceType,
            positions = positionsFut.join(),
            lastUpdated = complianceEntity.lastUpdated.time,
            aliases = aliasesFut.join(),
            addresses = addressesFut.join(),
            remarks = remarksFut.join(),
            sourceItems = sourceItemsFut.join(),
            riskSegments = riskSegmentsFut.join(),
            relatedEntities = relatedEntitiesFut.join(),
            countryAssociations = countryAssociationsFut.join(),
            identifications = identificationsFut.join(),
            additionalSegments = additionalSegmentsFut.join(),
            country = countryFut.join(),
            birthDate = birthDateFut.join(),
            type = complianceEntity.type
        )
    }




    override fun search(
        request: ComplianceSearchRequest,
        pageable: Pageable
    ): Any {
        val (firstname, lastname) = if (request.target == SearchTarget.INDIVIDUAL) {
            Pair("*${request.firstname?.dropEsChars()?.lowercase()}*", "*${request.lastname?.dropEsChars()?.lowercase()}*")

        } else {
            Pair("", "*${request.name?.dropEsChars()?.lowercase()}*")
        }

        return esCheckService.search(firstname,lastname, pageable, request.target)
    }


    private fun fetchRelatedEntities(entity: Compliance): List<ComplianceRelatedEntityDataRiskResponse> {
        if (entity.complianceType != ComplianceType.RELATIONSHIP) {
            return emptyList()
        }
        return relationshipRepository.findAllByCompliance(entity)
            .map(ComplianceRelatedEntityDataRiskResponse::toDto)
    }

    private fun fetchAliases(entity: Compliance) = entityAliasRepository.findAllByCompliance(entity)
        .map(ComplianceAliasResponse::toDto)

    private fun fetchAddresses(entity: Compliance) = addressRepository.findAllByCompliance(entity)
        .map(ComplianceAddressResponse::toDto)

    private fun fetchPositions(entity: Compliance) = positionRepository.findAllByCompliance(entity)
        .map(CompliancePositionDataRiskResponse::toDto)

    private fun fetchRemarks(entity: Compliance) = remarkRepository.findAllByCompliance(entity)
        .map(ComplianceRemarkResponse::toDto)

    private fun fetchSourceItems(entity: Compliance) = sourceItemRepository.findAllByCompliance(entity)
        .map(ComplianceSourceItemResponse::toDto)

    private fun fetchCountryAssociations(entity: Compliance): List<ComplianceCountryAssociationResponse> {
        return entityCountryAssociationRepository.findAllByCompliance(entity).map {
            ComplianceCountryAssociationResponse.toDto(it)
        }
    }

    private fun fetchIdentifications(entity: Compliance): List<ComplianceIdentificationResponse> {
        return entityIdentificationRepository.findAllByCompliance(entity).map {
            ComplianceIdentificationResponse.toDto(it)
        }
    }

    private fun fetchDob(entity: Compliance): LocalDate? {
        return entityDOBRepository.findAllByCompliance(entity).map {
            it.birthDate
        }.firstOrNull()
    }

    private fun fetchAdditionalSegments(entity: Compliance): List<ComplianceAdditionalSegmentResponse> {
        return additionalSegmentRepository.findAllByComplianceAndDeletedFalse(entity).map {
            val recordItems = mutableListOf<ComplianceRecordItemResponse>()
            recordRepository.findAllByAdditionalSegment(it).map { record ->
                fieldRepository.findAllByRecord(record).map { field ->
                    recordItems.add(ComplianceRecordItemResponse(field.id!!, field.derivedName, field.derivedValue))
                }
            }
            ComplianceAdditionalSegmentResponse(it.id!!, it.type, recordItems)
        }
    }


    private fun buildRiskSegments(complianceEntity: Compliance): EntitySegmentAdminResponse {
        val segmentFlags = entitySegmentRepository.findAllByComplianceAndDeletedFalse(complianceEntity).firstOrNull()
            ?: return EntitySegmentAdminResponse()

        return EntitySegmentAdminResponse(
            sanctions = fetchSegmentDataIfEnabled(segmentFlags.entitySanction) {
                buildSanctionsSegment(complianceEntity)
            },
            pep = fetchSegmentDataIfEnabled(segmentFlags.entityPep) {
                buildPepSegment(complianceEntity)
            },
            enforcement = fetchSegmentDataIfEnabled(segmentFlags.entityEnforcement) {
                buildEnforcementSegment(complianceEntity)
            },
            adverseMedia = fetchSegmentDataIfEnabled(segmentFlags.entityAdverseMedia) {
                buildAdverseMediaSegment(complianceEntity)
            },
            soe = fetchSegmentDataIfEnabled(segmentFlags.entitySOE) {
                buildSoeSegment(complianceEntity)
            }
        )
    }

    private fun <T> fetchSegmentDataIfEnabled(isEnabled: Boolean, fetcher: () -> List<T>): List<T> {
        return if (isEnabled) fetcher() else emptyList()
    }

    private fun buildSanctionsSegment(entity: Compliance): List<SanctionsSegmentResponse> {
        return entitySanctionRepository.findAllByCompliance(entity)
            .map(SanctionsSegmentResponse::toDto)
    }

    private fun buildPepSegment(entity: Compliance): List<PepSegmentResponse> {
        val peps = entityPepRepository.findAllByCompliance(entity)
        if (peps.isEmpty()) return emptyList()


        val pepIds = peps.map { it.id }
        val subcategoriesByPepId = entityPepSubCategoryRepository.findAllByEntityPepIdIn(pepIds)
            .groupBy { it.entityPep.id }

        return peps.map { pep ->
            val subCategoriesForPep = subcategoriesByPepId[pep.id]?.map { subCategory ->
                PepSegmentSubCategoryResponse(
                    subCategory.id,
                    subCategory.pepSubcategoryDesc,
                    subCategory.subCategoryLabel,
                    subCategory.lastUpdated?.time
                )
            } ?: emptyList()
            PepSegmentResponse.toDto(pep, subCategoriesForPep)
        }
    }

    private fun buildEnforcementSegment(entity: Compliance): List<EnforcementSegmentResponse> {
        val enforcementActions = entityEnforcementRepository.findAllByCompliance(entity)
        if (enforcementActions.isEmpty()) return emptyList()


        val enforcementIds = enforcementActions.map { it.id }
        val subcategoriesByEnforcementId =
            entityEnforcementSubcategoryRepository.findAllByEntityEnforcementIdIn(enforcementIds)
                .groupBy { it.entityEnforcement.id }

        return enforcementActions.map { enforcement ->
            val subcategoriesForEnforcement = subcategoriesByEnforcementId[enforcement.id]?.map { subCategory ->
                EnforcementSegmentSubCategoryResponse(
                    subCategory.id,
                    subCategory.subCategoryLabel,
                    subCategory.lastUpdated.time
                )
            } ?: emptyList()
            EnforcementSegmentResponse.toDto(enforcement, subcategoriesForEnforcement)
        }
    }

    private fun buildAdverseMediaSegment(entity: Compliance): List<AdverseMediaSegmentResponse> {
        val adverseMediaHits = entityAdverseMediaRepository.findAllByCompliance(entity)
        if (adverseMediaHits.isEmpty()) return emptyList()


        val mediaIds = adverseMediaHits.map { it.id }
        val subcategoriesByMediaId = adverseMediaSubCategoryRepository.findAllByEntityAdverseMediaIdIn(mediaIds)
            .groupBy { it.entityAdverseMedia.id }

        return adverseMediaHits.map { adverseMedia ->
            val subcategoriesForMedia = subcategoriesByMediaId[adverseMedia.id]?.map { subCategory ->
                AdverseMediaSubcategoryResponse(
                    subCategory.id,
                    subCategory.subCategoryLabel,
                    subCategory.lastUpdated?.time
                )
            } ?: emptyList()
            AdverseMediaSegmentResponse.toDto(adverseMedia, subcategoriesForMedia)
        }
    }

    private fun buildSoeSegment(entity: Compliance): List<SoeSegmentResponse> {
        val stateOwnedEntities = entitySoeRepository.findAllByCompliance(entity)
        if (stateOwnedEntities.isEmpty()) return emptyList()


        val soeIds = stateOwnedEntities.map { it.id }
        val subcategoriesBySoeId = entitySOESubcategoryRepository.findAllByEntitySOEIdIn(soeIds)
            .groupBy { it.entitySOE.id }
        val domainsBySoeId = entitySOEDomainRepository.findAllByEntitySOEIdIn(soeIds)
            .groupBy { it.entitySOE.id }

        return stateOwnedEntities.map { soe ->
            val subcategoriesForSoe = subcategoriesBySoeId[soe.id]?.map { subCategory ->
                SoeSegmentSubCategoryResponse(
                    subCategory.id,
                    subCategory.label,
                    subCategory.lastUpdated?.time
                )
            } ?: emptyList()

            val domainsForSoe = domainsBySoeId[soe.id]?.map { domain ->
                SoeSegmentDomainResponse(
                    domain.id,
                    domain.soeDomainDesc,
                    domain.lastUpdated.time
                )
            } ?: emptyList()

            SoeSegmentResponse.toDto(soe, subcategoriesForSoe, domainsForSoe)
        }
    }
}