package uz.davrbank.compliance.kafka

import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import mu.KotlinLogging
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import uz.davrbank.compliance.*
import uz.davrbank.compliance.backup.AdditionalDataToEntityDto
import uz.davrbank.compliance.backup.AdditionalSegment
import uz.davrbank.compliance.backup.AdditionalSegmentInternalDto
import uz.davrbank.compliance.backup.Compliance
import uz.davrbank.compliance.backup.ConsolidatedSanction
import uz.davrbank.compliance.backup.EntityAddress
import uz.davrbank.compliance.backup.EntityAdverseMedia
import uz.davrbank.compliance.backup.EntityAdverseMediaInternalDto
import uz.davrbank.compliance.backup.EntityAdverseMediaSubcategory
import uz.davrbank.compliance.backup.EntityAlias
import uz.davrbank.compliance.backup.EntityCountryAssociation
import uz.davrbank.compliance.backup.EntityDOB
import uz.davrbank.compliance.backup.EntityEnforcement
import uz.davrbank.compliance.backup.EntityEnforcementInternalDto
import uz.davrbank.compliance.backup.EntityEnforcementSubcategory
import uz.davrbank.compliance.backup.EntityIdentification
import uz.davrbank.compliance.backup.EntityPEP
import uz.davrbank.compliance.backup.EntityPEPSubcategory
import uz.davrbank.compliance.backup.EntityPepInternalDto
import uz.davrbank.compliance.backup.EntityPosition
import uz.davrbank.compliance.backup.EntityRelationship
import uz.davrbank.compliance.backup.EntityRemark
import uz.davrbank.compliance.backup.EntitySOE
import uz.davrbank.compliance.backup.EntitySOEDomain
import uz.davrbank.compliance.backup.EntitySOESubcategory
import uz.davrbank.compliance.backup.EntitySOEsInternalDto
import uz.davrbank.compliance.backup.EntitySanction
import uz.davrbank.compliance.backup.EntitySanctionInternalDto
import uz.davrbank.compliance.backup.EntitySegmentDto
import uz.davrbank.compliance.backup.EntitySourceItem
import uz.davrbank.compliance.backup.Field
import uz.davrbank.compliance.backup.PGBackupService
import uz.davrbank.compliance.backup.Record
import uz.davrbank.compliance.backup.RecordInternalDto
import uz.davrbank.compliance.elasticsearch.NewComplianceData
import uz.davrbank.compliance.utils.DateConversionUtils
import java.util.UUID


interface KafkaDataSender {
    fun sendFileData(data: FileKafkaDto)
}


@Component
class EntityDataExtractor {
    private val logger = KotlinLogging.logger {}


    fun toComplianceModels(
        dto: ComplianceEntityDto,
        complianceType: ComplianceType
    ): Pair<NewComplianceData, Compliance> {
        val firstname = dto.firstName?.dropEsChars()?.lowercase() ?: ""
        val lastname = dto.lastName?.dropEsChars()?.lowercase() ?: ""
        val fullName = buildFullName(firstname, lastname).lowercase()
        val country = dto
            .entityCountryAssociations?.entityCountryAssociation?.filter { it.associationTypeDesc == "Citizenship" || it.associationTypeDesc == "Nationality" }
            ?.associateBy { it.isostandard }
            ?.values?.toList()
            ?.firstOrNull()?.administrativeUnitName ?: ""

        val birth = dto.entityDobs?.entityDob?.map { dob ->
             DateConversionUtils.createLocalDate(
                dob.birthDate?.birthYear,
                dob.birthDate?.birthMonth,
                dob.birthDate?.birthDay
            )
        }?.firstOrNull()



        val complianceEntityDoc = NewComplianceData(
            firstname = firstname,
            lastname = lastname,
            fullName = fullName,
            extLookUpId = dto.lookupId ?: "",
            type = dto.entityTypeDesc.name,
            complianceType = complianceType,
            id = dto.entityGuid,
            dataType = DataType.LEXIS,
            region = country ,
            birth = birth?.let { localDateToString(it) }
        )

        val deceasedDate = DateConversionUtils.createLocalDate(
            dto.deceasedDate?.deceasedYear,
            dto.deceasedDate?.deceasedMonth,
            dto.deceasedDate?.deceasedDay
        )

        val complianceEntity = Compliance(
            id = dto.entityGuid,
            type = dto.entityTypeDesc,
            gender = dto.gender,
            name = dto.name,
            firstName = dto.firstName,
            middleName = dto.middleName,
            lastName = dto.lastName ?: "",
            fullName = fullName,
            prefix = dto.prefix,
            suffix = dto.suffix,
            title = dto.title,
            isDeceased = dto.isDeceased,
            deceasedDate = deceasedDate,
            lastUpdated = DateConversionUtils.parseTimestamp(dto.lastUpdated),
            entityId = dto.entityId,
            lookupId = dto.lookupId,
            complianceType = complianceType,
            dataType = DataType.LEXIS
        )

        return complianceEntityDoc to complianceEntity
    }


    fun extractAdditionalData(
        dto: ComplianceEntityDto,
        entity: Compliance,
        complianceType: ComplianceType
    ): AdditionalDataToEntityDto {
        return AdditionalDataToEntityDto(
            entityDOBs = extractEntityDOBs(dto, entity).toMutableList(),
            entityAddresses = extractEntityAddresses(dto, entity).toMutableList(),
            entityCountryAssociations = extractCountryAssociations(dto, entity, complianceType).toMutableList(),
            entityPositions = extractPositions(dto, entity).toMutableList(),
            entityRelationships = extractRelationships(dto, entity, complianceType).toMutableList(),
            entityRemarks = extractRemarks(dto, entity).toMutableList(),
            entityAlias = extractAliases(dto, entity).toMutableList(),
            entityIdentification = extractIdentifications(dto, entity).toMutableList(),
            entitySourceItem = extractSourceItems(dto, entity).toMutableList(),
            entitySegment = extractSegmentData(dto, entity, complianceType),
            entityComplianceEntity = entity,
        )
    }


    private fun extractEntityDOBs(dto: ComplianceEntityDto, entity: Compliance): List<EntityDOB> {
        return dto.entityDobs?.entityDob?.map { dob ->
            val birthDate = DateConversionUtils.createLocalDate(
                dob.birthDate?.birthYear,
                dob.birthDate?.birthMonth,
                dob.birthDate?.birthDay
            )

            EntityDOB(
                id = dob.entityDobguid,
                birthDate = birthDate,
                lastUpdated = DateConversionUtils.parseTimestamp(dob.lastUpdated),
                compliance = entity
            )
        } ?: emptyList()
    }

    private fun extractEntityAddresses(dto: ComplianceEntityDto, entity: Compliance): List<EntityAddress> {
        return dto.entityAddresses?.entityAddress?.mapNotNull { address ->
            address.entityAddressGuid?.let { guid ->
                EntityAddress(
                    id = guid,
                    addressTypeDesc = address.addressTypeDesc,
                    city = address.city,
                    stateProvinceRegion = address.stateProvinceRegion,
                    country = address.country,
                    isoStandard = address.isostandard,
                    lastUpdated = DateConversionUtils.parseTimestamp(address.lastUpdated),
                    postalCode = address.postalCode,
                    address1 = address.address1,
                    address2 = address.address2,
                    compliance = entity
                )
            }
        } ?: emptyList()
    }

    private fun extractCountryAssociations(
        dto: ComplianceEntityDto,
        entity: Compliance,
        complianceType: ComplianceType
    ): List<EntityCountryAssociation> {
        return dto.entityCountryAssociations?.entityCountryAssociation?.mapNotNull { country ->
            country.entityCountryAssociationGuid?.let { guid ->
                EntityCountryAssociation(
                    compliance = entity,
                    id = guid,
                    administrativeUnitName = country.administrativeUnitName,
                    associationTypeDesc = country.associationTypeDesc,
                    isostandard = country.isostandard,
                    lastUpdated = DateConversionUtils.parseTimestamp(country.lastUpdated),
                    complianceType = complianceType
                )
            }
        } ?: emptyList()
    }

    private fun extractPositions(dto: ComplianceEntityDto, entity: Compliance): List<EntityPosition> {
        val position = dto.entityPositions?.entityPosition ?: return emptyList()

        return listOfNotNull(
            position.entityPositionGuid?.let { guid ->
                EntityPosition(
                    id = guid,
                    position = position.position,
                    lastUpdated = DateConversionUtils.parseTimestamp(position.lastUpdated),
                    compliance = entity
                )
            }
        )
    }

    private fun extractRelationships(
        dto: ComplianceEntityDto,
        entity: Compliance,
        complianceType: ComplianceType
    ): List<EntityRelationship> {
        return dto.entityRelationships?.entityRelationship?.mapNotNull { rel ->
            rel.entityRelationshipGuid?.let { guid ->
                EntityRelationship(
                    id = guid,
                    relatedEntityGUID = rel.relatedEntityGuid,
                    groupDesc = rel.groupDesc,
                    relationshipDesc = rel.relationshipDesc,
                    lastUpdated = DateConversionUtils.parseTimestamp(rel.lastUpdated),
                    compliance = entity,
                    complianceType = complianceType
                )
            }
        } ?: emptyList()
    }

    private fun extractRemarks(dto: ComplianceEntityDto, entity: Compliance): List<EntityRemark> {
        return dto.entityRemarks?.entityRemark?.mapNotNull { remark ->
            remark.entityRemarkGuid?.let { guid ->
                EntityRemark(
                    id = guid,
                    remark = remark.remark,
                    lastUpdated = DateConversionUtils.parseTimestamp(dto.lastUpdated),
                    compliance = entity
                )
            }
        } ?: emptyList()
    }

    private fun extractAliases(dto: ComplianceEntityDto, entity: Compliance): List<EntityAlias> {
        return dto.entityAliases?.entityAlias?.mapNotNull { alias ->
            alias.entityAliasGuid?.let { guid ->
                EntityAlias(
                    id = guid,
                    aliasTypeDesc = alias.aliasTypeDesc,
                    englishDescription = alias.englishDescription,
                    firstName = alias.firstName,
                    lastName = alias.lastName,
                    lastUpdated = DateConversionUtils.parseTimestamp(alias.lastUpdated),
                    middleName = alias.middleName,
                    name = alias.name,
                    prefix = alias.prefix,
                    suffix = alias.suffix,
                    compliance = entity
                )
            }
        } ?: emptyList()
    }

    private fun extractIdentifications(dto: ComplianceEntityDto, entity: Compliance): List<EntityIdentification> {
        return dto.entityIdentifications?.entityIdentification?.mapNotNull { id ->
            id.entityIdentificationGuid?.let { guid ->
                val issueDate = DateConversionUtils.createLocalDate(
                    id.issueDate.issueYear,
                    id.issueDate.issueMonth,
                    id.issueDate.issueDay
                )
                val expirationDate = DateConversionUtils.createLocalDate(
                    id.expirationDate.expirationYear,
                    id.expirationDate.expirationMonth,
                    id.expirationDate.expirationDay
                )

                EntityIdentification(
                    id = guid,
                    administrativeUnitName = id.administrativeUnitName,
                    expiration = expirationDate,
                    isoStandard = id.isostandard,
                    identificationIssuer = id.identificationIssuer,
                    identificationNumber = id.identificationNumber,
                    identificationTypeDesc = id.identificationTypeDesc,
                    issueDate = issueDate,
                    lastUpdated = DateConversionUtils.parseTimestamp(id.lastUpdated),
                    compliance = entity
                )
            }
        } ?: emptyList()
    }

    private fun extractSourceItems(dto: ComplianceEntityDto, entity: Compliance): List<EntitySourceItem> {
        return dto.entitySourceItems?.entitySourceItem?.mapNotNull { item ->
            item.entitySourceItemGuid?.let { guid ->
                EntitySourceItem(
                    id = guid,
                    sourceURI = item.sourceUri,
                    lastUpdated = DateConversionUtils.parseTimestamp(dto.lastUpdated),
                    compliance = entity
                )
            }
        } ?: emptyList()
    }

    private fun extractSegmentData(
        dto: ComplianceEntityDto,
        entity: Compliance,
        complianceType: ComplianceType
    ): EntitySegmentDto {
        return EntitySegmentDto(
            entityPepInternal = extractPEPSegments(dto, entity).toMutableList(),
            entityAdverseMediaInternalDto = extractAdverseMediaSegments(dto, entity).toMutableList(),
            entityEnforcementInternalDto = extractEnforcementSegments(dto, entity).toMutableList(),
            entitySOEsInternalDto = extractSOESegments(dto, entity).toMutableList(),
            entitySanctionInternalDto = extractSanctionSegments(dto, entity, complianceType).toMutableList(),
            additionalSegmentInternalDto = extractAdditionalSegments(dto, entity).toMutableList()
        )
    }


    private fun extractPEPSegments(dto: ComplianceEntityDto, entity: Compliance): List<EntityPepInternalDto> {

        return dto.entitySegments?.entityPeps?.entityPep?.map { pepInternal ->
            val effectiveYear = DateConversionUtils.createLocalDate(
                pepInternal.effectiveDate?.effectiveYear,
                pepInternal.effectiveDate?.effectiveMonth,
                pepInternal.effectiveDate?.effectiveDay
            )

            val expirationYear = DateConversionUtils.createLocalDate(
                pepInternal.expirationDate?.expirationYear,
                pepInternal.expirationDate?.expirationMonth,
                pepInternal.expirationDate?.expirationDay
            )


            val entityPep = EntityPEP(
                pepInternal.entityPepguid!!,
                pepInternal.isPrimaryPep,
                pepInternal.isActivePep,
                pepInternal.isInCountryPeponly,
                pepInternal.pepadminLevelDesc,
                pepInternal.isoadministrativeUnitLevel0,
                pepInternal.administrativeUnitLevel0,
                pepInternal.governingInstitution,
                pepInternal.governingRole,
                pepInternal.lastUpdated?.let { DateConversionUtils.parseTimestamp(it) },
                effectiveYear,
                expirationYear,
                entity
            )
            var subcategory: EntityPEPSubcategory
            pepInternal.entityPepsubcategories?.subcategory.let { pepSubCategory ->
                subcategory = EntityPEPSubcategory(
                    entityPep,
                    pepSubCategory?.entityPepsubCategoryGuid ?: "custom_${UUID.randomUUID()}",
                    pepSubCategory?.subCategoryDesc,
                    pepSubCategory?.subCategoryLabel,
                    pepSubCategory?.lastUpdated?.let { DateConversionUtils.parseTimestamp(it) },
                    entity
                )
            }

            EntityPepInternalDto(entityPep, subcategory)
        } ?: emptyList()
    }

    private fun extractAdverseMediaSegments(
        dto: ComplianceEntityDto,
        entity: Compliance
    ): List<EntityAdverseMediaInternalDto> {
        return dto.entitySegments?.entityAdverseMedias?.entityAdverseMedia?.map { adverseMedia ->
            val entityAdverseMedia = EntityAdverseMedia(
                adverseMedia.entityAdverseMediaGuid!!,
                adverseMedia.adverseMediaDesc,
                adverseMedia.lastUpdated?.let { DateConversionUtils.parseTimestamp(it) },
                entity
            )

            val entityAdverseMediaSubcategory = adverseMedia
                .entityAdverseMediaSubcategories
                ?.subcategory
                ?.map { adverseMediaSubCategory ->

                    EntityAdverseMediaSubcategory(
                        entityAdverseMedia,
                        adverseMediaSubCategory.entityAdverseMediaSubCategoryGuid!!,
                        adverseMediaSubCategory.subCategoryLabel,
                        adverseMediaSubCategory.lastUpdated?.let { DateConversionUtils.parseTimestamp(it) },
                        entity
                    )
                } ?: emptyList()

            EntityAdverseMediaInternalDto(
                entityAdverseMedia, entityAdverseMediaSubcategory
            )
        } ?: emptyList()
    }

    private fun extractEnforcementSegments(
        dto: ComplianceEntityDto,
        entity: Compliance
    ): List<EntityEnforcementInternalDto> {
        return dto
            .entitySegments
            ?.entityEnforcements
            ?.entityEnforcement
            ?.map { enforcement ->
                val entityEnforcement = EntityEnforcement(
                    enforcement.entityEnforcementGUID,
                    enforcement.enforcementDesc,
                    enforcement.sourceName,
                    enforcement.sourceNameAbbrev,
                    enforcement.administrativeUnitName,
                    enforcement.isoStandard,
                    DateConversionUtils.parseTimestamp(enforcement.lastUpdated),
                    entity
                )

                val subcategories = enforcement
                    .entityEnforcementSubcategories
                    .subcategory
                    .map { subcategory ->
                        EntityEnforcementSubcategory(
                            entityEnforcement,
                            subcategory.entityEnforcementSubCategoryGUID,
                            subcategory.subCategoryLabel,
                            DateConversionUtils.parseTimestamp(subcategory.lastUpdated),
                            entity
                        )
                    }


                EntityEnforcementInternalDto(
                    entityEnforcement, subcategories
                )

            } ?: emptyList()
    }

    private fun extractSOESegments(dto: ComplianceEntityDto, entity: Compliance): List<EntitySOEsInternalDto> {
        return dto
            .entitySegments
            ?.entitySOEs
            ?.entitySOE
            ?.map { soe ->
                val entitySoe = EntitySOE(
                    entity,
                    soe.entitySOEGUID,
                    soe.isActive,
                    DateConversionUtils.parseTimestamp(soe.lastUpdated)
                )

                val entitySOESubcategory = soe
                    .entitySOESubcategories
                    ?.subcategory
                    ?.map { soeSubCategory ->
                        EntitySOESubcategory(
                            entitySoe,
                            soeSubCategory.entitySOESubcategoryGUID!!,
                            soeSubCategory.subCategoryLabel,
                            soeSubCategory.lastUpdated?.let { DateConversionUtils.parseTimestamp(it) },
                            entity
                        )
                    } ?: emptyList()

                val entitySOEDomains = soe
                    .entitySOEDomains
                    ?.entitySOEDomain
                    ?.map { soeDomain ->
                        EntitySOEDomain(
                            entitySoe,
                            soeDomain.entitySOEDomainGUID,
                            soeDomain.soeDomainDesc,
                            DateConversionUtils.parseTimestamp(soeDomain.lastUpdated),
                            entity
                        )
                    } ?: emptyList()

                EntitySOEsInternalDto(entitySoe, entitySOESubcategory, entitySOEDomains)
            } ?: emptyList()
    }

    private fun extractSanctionSegments(
        dto: ComplianceEntityDto,
        entity: Compliance,
        complianceType: ComplianceType
    ): List<EntitySanctionInternalDto> {
        return dto
            .entitySegments
            ?.entitySanctions
            ?.entitySanction
            ?.map { sanction ->
                val entitySanction = EntitySanction(
                    entity,
                    sanction.entitySanctionGUID,
                    sanction.subCategoryLabel,
                    sanction.sourceName,
                    sanction.sourceNameAbbrev,
                    sanction.administrativeUnitName,
                    sanction.isoStandard,
                    DateConversionUtils.parseTimestamp(sanction.lastUpdated),
                    complianceType = complianceType,
                    ConsolidatedSanction(
                        sanction.consolidatedSanction?.consolidatedSanctionGUID,
                        sanction.consolidatedSanction?.entityGUID,
                        DateConversionUtils.parseTimestamp(sanction.lastUpdated)
                    )
                )

                EntitySanctionInternalDto(entitySanction)
            } ?: emptyList()
    }

    private fun extractAdditionalSegments(
        dto: ComplianceEntityDto,
        entity: Compliance
    ): List<AdditionalSegmentInternalDto> {
        return dto
            .entitySegments
            ?.additionalSegments
            ?.segment?.map { segment ->
                val additionalSegment =
                    AdditionalSegment(
                        entity,
                        segment.type
                    )
                val recordKeeper = mutableListOf<RecordInternalDto>()
                segment
                    .record
                    ?.forEach { record ->

                        val recordEntity = Record(
                            additionalSegment,
                            DateConversionUtils
                                .parseTimestamp(record.lastUpdated),
                            record.guid!!,
                            entity
                        )

                        val fields = record
                            .field
                            ?.map { field ->
                                Field(
                                    recordEntity,
                                    field.derivedName,
                                    field.derivedValue,
                                    entity
                                )
                            } ?: emptyList()
                        recordKeeper.add(RecordInternalDto(recordEntity, fields))
                    }

                AdditionalSegmentInternalDto(
                    additionalSegment, recordKeeper
                )
            } ?: emptyList()
    }

    private fun buildFullName(firstname: String, lastname: String): String {
        return buildString {
            if (firstname.isNotBlank()) append(firstname)
            if (lastname.isNotBlank()) {
                if (isNotEmpty()) append("")
                append(lastname)
            }
        }
    }
}

@Service
class DataSenderImpl(
    val kafkaTemplate: KafkaTemplate<String, Any>,
) : KafkaDataSender {
    val logger = LoggerFactory.getLogger(KafkaDataSender::class.java)
    override fun sendFileData(data: FileKafkaDto) {
        kafkaTemplate.send(FILE_DATA_SEND, data)
        logger.info("File data has been sent to Kafka")
    }

}


interface KafkaDataService {
    fun saveEntityDeleteData(message: DeleteEventKafka)
    fun deleteEntityData()
}


@Service
class KafkaDataServiceImpl(
    private val entityDeleteDataStoreRepository: EntityDeleteDataStoreRepository,
    private val pgBackupService: PGBackupService
) : KafkaDataService {
    override fun saveEntityDeleteData(message: DeleteEventKafka) {
        entityDeleteDataStoreRepository.save(
            EntityDeleteDataStore(
                message.EntityGUID,
                message.DateDeleted
            )
        )
    }


    @Scheduled(fixedDelay = 30_000)
    override fun deleteEntityData() = runBlocking {
        logger.info("STARTING DELETE ENTITY JOB...")


        val itemsToDelete = entityDeleteDataStoreRepository.findAllByJobFinishedIsFalseOrderByCreatedDate().take(10)

        if (itemsToDelete.isEmpty()) {
            logger.info("No entities to delete. Job finished.")
            return@runBlocking
        }

        logger.info("Found ${itemsToDelete.size} entities to delete. Processing in parallel...")


        val concurrencyLimit = 5
        val semaphore = Semaphore(concurrencyLimit)


        coroutineScope {
            itemsToDelete.forEach { entityDeleteDataStore ->
                if (entityDeleteDataStore.tryCount > 3) {
                    return@forEach
                }
                launch(Dispatchers.IO) {
                    semaphore.withPermit {
                        try {
                            pgBackupService.deleteEntity(entityDeleteDataStore.complianceId)
                            entityDeleteDataStore.jobFinished = true
                            entityDeleteDataStoreRepository.save(entityDeleteDataStore)
                            logger.debug { "Successfully deleted and marked as finished for ID: ${entityDeleteDataStore.id}" }
                        } catch (ex: Exception) {

                            logger.error(
                                "Failed to delete entity for ID: ${entityDeleteDataStore.id}. Error: ${ex.message}",
                                ex
                            )
                            entityDeleteDataStore.tryCount += 1
                            entityDeleteDataStore.errorMessage = ex.message?.take(255)
                            entityDeleteDataStoreRepository.save(entityDeleteDataStore)
                        }
                    }
                }
            }
        }

        logger.info("SUCCESSFULLY FINISHED DELETE ENTITY JOB for ${itemsToDelete.size} items.")
    }
}