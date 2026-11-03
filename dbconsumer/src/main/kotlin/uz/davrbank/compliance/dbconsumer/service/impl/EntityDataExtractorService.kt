package uz.davrbank.compliance.dbconsumer.service.impl

import org.springframework.stereotype.Component
import uz.davrbank.compliance.dbconsumer.domain.elasticsearch.NewComplianceData
import uz.davrbank.compliance.dbconsumer.domain.postgresql.AdditionalSegment
import uz.davrbank.compliance.dbconsumer.domain.postgresql.Compliance
import uz.davrbank.compliance.dbconsumer.domain.postgresql.ConsolidatedSanction
import uz.davrbank.compliance.dbconsumer.domain.postgresql.EntityAddress
import uz.davrbank.compliance.dbconsumer.domain.postgresql.EntityAdverseMedia
import uz.davrbank.compliance.dbconsumer.domain.postgresql.EntityAdverseMediaSubcategory
import uz.davrbank.compliance.dbconsumer.domain.postgresql.EntityAlias
import uz.davrbank.compliance.dbconsumer.domain.postgresql.EntityCountryAssociation
import uz.davrbank.compliance.dbconsumer.domain.postgresql.EntityDOB
import uz.davrbank.compliance.dbconsumer.domain.postgresql.EntityEnforcement
import uz.davrbank.compliance.dbconsumer.domain.postgresql.EntityEnforcementSubcategory
import uz.davrbank.compliance.dbconsumer.domain.postgresql.EntityIdentification
import uz.davrbank.compliance.dbconsumer.domain.postgresql.EntityPEP
import uz.davrbank.compliance.dbconsumer.domain.postgresql.EntityPEPSubcategory
import uz.davrbank.compliance.dbconsumer.domain.postgresql.EntityPosition
import uz.davrbank.compliance.dbconsumer.domain.postgresql.EntityRelationship
import uz.davrbank.compliance.dbconsumer.domain.postgresql.EntityRemark
import uz.davrbank.compliance.dbconsumer.domain.postgresql.EntitySOE
import uz.davrbank.compliance.dbconsumer.domain.postgresql.EntitySOEDomain
import uz.davrbank.compliance.dbconsumer.domain.postgresql.EntitySOESubcategory
import uz.davrbank.compliance.dbconsumer.domain.postgresql.EntitySanction
import uz.davrbank.compliance.dbconsumer.domain.postgresql.EntitySourceItem
import uz.davrbank.compliance.dbconsumer.domain.postgresql.Field
import uz.davrbank.compliance.dbconsumer.domain.postgresql.Record
import uz.davrbank.compliance.dbconsumer.dto.internal.AdditionalDataToEntityDto
import uz.davrbank.compliance.dbconsumer.dto.internal.AdditionalSegmentInternalDto
import uz.davrbank.compliance.dbconsumer.dto.internal.EntityAdverseMediaInternalDto
import uz.davrbank.compliance.dbconsumer.dto.internal.EntityEnforcementInternalDto
import uz.davrbank.compliance.dbconsumer.dto.internal.EntityPepInternalDto
import uz.davrbank.compliance.dbconsumer.dto.internal.EntitySOEsInternalDto
import uz.davrbank.compliance.dbconsumer.dto.internal.EntitySanctionInternalDto
import uz.davrbank.compliance.dbconsumer.dto.internal.EntitySegmentDto
import uz.davrbank.compliance.dbconsumer.dto.internal.RecordInternalDto
import uz.davrbank.compliance.dbconsumer.dto.kafka.ComplianceEntityDto
import uz.davrbank.compliance.dbconsumer.enums.ComplianceType
import uz.davrbank.compliance.dbconsumer.enums.DataType
import uz.davrbank.compliance.dbconsumer.util.DateConversionUtils
import uz.davrbank.compliance.dbconsumer.util.dropEsChars
import uz.davrbank.compliance.dbconsumer.util.localDateToString
import java.util.UUID
import kotlin.collections.emptyList

@Component
class EntityDataExtractorService {

    fun toComplianceModels(
        dto: ComplianceEntityDto,
        complianceType: ComplianceType
    ): Pair<NewComplianceData, Compliance> {
        val firstname = dto.firstName?.dropEsChars()?.lowercase() ?: ""
        val lastname  = dto.lastName?.dropEsChars()?.lowercase() ?: ""
        val fullName  = buildFullName(firstname, lastname).lowercase()
        val country   = dto.entityCountryAssociations?.entityCountryAssociation
            ?.filter { it.associationTypeDesc == "Citizenship" || it.associationTypeDesc == "Nationality" }
            ?.associateBy { it.isostandard }
            ?.values?.toList()
            ?.firstOrNull()?.administrativeUnitName ?: ""

        val birth = dto.entityDobs?.entityDob?.map { dob ->
            DateConversionUtils.createLocalDate(dob.birthDate?.birthYear, dob.birthDate?.birthMonth, dob.birthDate?.birthDay)
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
            region = country,
            birth = birth?.let { localDateToString(it) },
            entityAdverseMedia = (dto.entitySegments?.entityAdverseMedias?.entityAdverseMedia?.size ?: 0) > 0,
            name = dto.name,
            identificationNumber = dto.entityIdentifications?.entityIdentification?.get(0)?.identificationNumber
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
        return dto.entityDobs?.entityDob?.mapNotNull { dob ->
            dob.entityDobguid?.let { guid ->
                EntityDOB(
                    id = guid,
                    birthDate = DateConversionUtils.createLocalDate(
                        dob.birthDate?.birthYear,
                        dob.birthDate?.birthMonth,
                        dob.birthDate?.birthDay
                    ),
                    lastUpdated = DateConversionUtils.parseTimestamp(dob.lastUpdated),
                    compliance = entity
                )
            }
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
                EntityIdentification(
                    id = guid,
                    administrativeUnitName = id.administrativeUnitName,
                    expiration = DateConversionUtils.createLocalDate(
                        id.expirationDate.expirationYear,
                        id.expirationDate.expirationMonth,
                        id.expirationDate.expirationDay
                    ),
                    isoStandard = id.isostandard,
                    identificationIssuer = id.identificationIssuer,
                    identificationNumber = id.identificationNumber,
                    identificationTypeDesc = id.identificationTypeDesc,
                    issueDate = DateConversionUtils.createLocalDate(
                        id.issueDate.issueYear,
                        id.issueDate.issueMonth,
                        id.issueDate.issueDay
                    ),
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
            val effectiveDate   = DateConversionUtils.createLocalDate(pepInternal.effectiveDate?.effectiveYear, pepInternal.effectiveDate?.effectiveMonth, pepInternal.effectiveDate?.effectiveDay)
            val expirationDate  = DateConversionUtils.createLocalDate(pepInternal.expirationDate?.expirationYear, pepInternal.expirationDate?.expirationMonth, pepInternal.expirationDate?.expirationDay)

            val entityPep = EntityPEP(
                id = pepInternal.entityPepguid!!,
                isPrimaryPEP = pepInternal.isPrimaryPep,
                isActivePEP = pepInternal.isActivePep,
                isInCountryPEPOnly = pepInternal.isInCountryPeponly,
                pepAdminLevelDesc = pepInternal.pepadminLevelDesc,
                isoAdministrativeUnitLevel0 = pepInternal.isoadministrativeUnitLevel0,
                administrativeUnitLevel0 = pepInternal.administrativeUnitLevel0,
                governingInstitution = pepInternal.governingInstitution,
                governingRole = pepInternal.governingRole,
                lastUpdated = pepInternal.lastUpdated?.let { DateConversionUtils.parseTimestamp(it) },
                effectiveDate = effectiveDate,
                expirationDate = expirationDate,
                compliance = entity
            )

            val pepSubCategory = pepInternal.entityPepsubcategories?.subcategory
            val subcategory = EntityPEPSubcategory(
                entityPep = entityPep,
                id = pepSubCategory?.entityPepsubCategoryGuid ?: "custom_${UUID.randomUUID()}",
                pepSubcategoryDesc = pepSubCategory?.subCategoryDesc,
                subCategoryLabel = pepSubCategory?.subCategoryLabel,
                lastUpdated = pepSubCategory?.lastUpdated?.let { DateConversionUtils.parseTimestamp(it) },
                compliance = entity
            )

            EntityPepInternalDto(entityPep, subcategory)
        } ?: emptyList()
    }

    private fun extractAdverseMediaSegments(dto: ComplianceEntityDto, entity: Compliance): List<EntityAdverseMediaInternalDto> {
        return dto.entitySegments?.entityAdverseMedias?.entityAdverseMedia?.map { adverseMedia ->
            val entityAdverseMedia = EntityAdverseMedia(
                id = adverseMedia.entityAdverseMediaGuid!!,
                adverseMediaDesc = adverseMedia.adverseMediaDesc,
                lastUpdated = adverseMedia.lastUpdated?.let { DateConversionUtils.parseTimestamp(it) },
                compliance = entity
            )
            val subcategories = adverseMedia.entityAdverseMediaSubcategories?.subcategory?.map { sub ->
                EntityAdverseMediaSubcategory(
                    entityAdverseMedia = entityAdverseMedia,
                    id = sub.entityAdverseMediaSubCategoryGuid!!,
                    subCategoryLabel = sub.subCategoryLabel,
                    lastUpdated = sub.lastUpdated?.let { DateConversionUtils.parseTimestamp(it) },
                    compliance = entity
                )
            } ?: emptyList()
            EntityAdverseMediaInternalDto(entityAdverseMedia, subcategories)
        } ?: emptyList()
    }

    private fun extractEnforcementSegments(dto: ComplianceEntityDto, entity: Compliance): List<EntityEnforcementInternalDto> {
        return dto.entitySegments?.entityEnforcements?.entityEnforcement?.mapNotNull { enforcement ->
            enforcement.entityEnforcementGUID?.let { guid ->
                val entityEnforcement = EntityEnforcement(
                    id = guid,
                    enforcementDesc = enforcement.enforcementDesc,
                    sourceName = enforcement.sourceName,
                    sourceNameAbbrev = enforcement.sourceNameAbbrev,
                    administrativeUnitName = enforcement.administrativeUnitName,
                    isoStandard = enforcement.isoStandard,
                    lastUpdated = DateConversionUtils.parseTimestamp(enforcement.lastUpdated),
                    compliance = entity
                )
                val subcategories = enforcement.entityEnforcementSubcategories.subcategory.map { sub ->
                    EntityEnforcementSubcategory(
                        entityEnforcement = entityEnforcement,
                        id = sub.entityEnforcementSubCategoryGUID,
                        subCategoryLabel = sub.subCategoryLabel,
                        lastUpdated = DateConversionUtils.parseTimestamp(sub.lastUpdated),
                        compliance = entity
                    )
                }
                EntityEnforcementInternalDto(entityEnforcement, subcategories)
            }
        } ?: emptyList()
    }

    private fun extractSOESegments(dto: ComplianceEntityDto, entity: Compliance): List<EntitySOEsInternalDto> {
        return dto.entitySegments?.entitySOEs?.entitySOE?.map { soe ->
            val entitySoe = EntitySOE(
                compliance = entity,
                id = soe.entitySOEGUID,
                isActive = soe.isActive,
                lastUpdated = DateConversionUtils.parseTimestamp(soe.lastUpdated)
            )
            val subcategories = soe.entitySOESubcategories?.subcategory?.map { sub ->
                EntitySOESubcategory(
                    entitySOE = entitySoe,
                    id = sub.entitySOESubcategoryGUID!!,
                    label = sub.subCategoryLabel,
                    lastUpdated = sub.lastUpdated?.let { DateConversionUtils.parseTimestamp(it) },
                    compliance = entity
                )
            } ?: emptyList()
            val domains = soe.entitySOEDomains?.entitySOEDomain?.map { domain ->
                EntitySOEDomain(
                    entitySOE = entitySoe,
                    id = domain.entitySOEDomainGUID,
                    soeDomainDesc = domain.soeDomainDesc,
                    lastUpdated = DateConversionUtils.parseTimestamp(domain.lastUpdated),
                    compliance = entity
                )
            } ?: emptyList()
            EntitySOEsInternalDto(entitySoe, subcategories, domains)
        } ?: emptyList()
    }

    private fun extractSanctionSegments(
        dto: ComplianceEntityDto,
        entity: Compliance,
        complianceType: ComplianceType
    ): List<EntitySanctionInternalDto> {
        return dto.entitySegments?.entitySanctions?.entitySanction?.mapNotNull { sanction ->
            sanction.entitySanctionGUID?.let { guid ->
                val entitySanction = EntitySanction(
                    compliance = entity,
                    id = guid,
                    subCategoryLabel = sanction.subCategoryLabel,
                    sourceName = sanction.sourceName,
                    sourceNameAbbrev = sanction.sourceNameAbbrev,
                    administrativeUnitName = sanction.administrativeUnitName,
                    isoStandard = sanction.isoStandard,
                    lastUpdated = DateConversionUtils.parseTimestamp(sanction.lastUpdated),
                    complianceType = complianceType,
                    consolidatedSanction = ConsolidatedSanction(
                        consolidatedSanctionGUID = sanction.consolidatedSanction?.consolidatedSanctionGUID,
                        entityGUID = sanction.consolidatedSanction?.entityGUID,
                        lastUpdated = DateConversionUtils.parseTimestamp(sanction.lastUpdated)
                    )
                )
                EntitySanctionInternalDto(entitySanction)
            }
        } ?: emptyList()
    }

    private fun extractAdditionalSegments(dto: ComplianceEntityDto, entity: Compliance): List<AdditionalSegmentInternalDto> {
        return dto.entitySegments?.additionalSegments?.segment?.map { segment ->
            val additionalSegment = AdditionalSegment(compliance = entity, type = segment.type)
            val records = segment.record?.mapNotNull { record ->
                val guid = record.guid ?: return@mapNotNull null
                val recordEntity = Record(
                    additionalSegment = additionalSegment,
                    lastUpdated = DateConversionUtils.parseTimestamp(record.lastUpdated),
                    id = guid,
                    compliance = entity
                )
                val fields = record.field?.map { field ->
                    Field(
                        record = recordEntity,
                        derivedName = field.derivedName,
                        derivedValue = field.derivedValue,
                        compliance = entity
                    )
                } ?: emptyList()
                RecordInternalDto(recordEntity, fields)
            } ?: emptyList()
            AdditionalSegmentInternalDto(additionalSegment, records)
        } ?: emptyList()
    }

    private fun buildFullName(firstname: String, lastname: String): String {
        return buildString {
            if (firstname.isNotBlank()) append(firstname)
            if (lastname.isNotBlank()) {
                if (isNotEmpty()) append(" ")
                append(lastname)
            }
        }
    }
}