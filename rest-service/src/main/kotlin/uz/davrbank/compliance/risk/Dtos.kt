package uz.davrbank.compliance.risk


import uz.davrbank.compliance.ComplianceType
import uz.davrbank.compliance.backup.ComplianceRelatedEntityDataRiskResponse
import uz.davrbank.compliance.backup.EntityAddress
import uz.davrbank.compliance.backup.EntityAdverseMedia
import uz.davrbank.compliance.backup.EntityAlias
import uz.davrbank.compliance.backup.EntityCountryAssociation
import uz.davrbank.compliance.backup.EntityEnforcement
import uz.davrbank.compliance.backup.EntityIdentification
import uz.davrbank.compliance.backup.EntityPEP
import uz.davrbank.compliance.backup.EntityPosition
import uz.davrbank.compliance.backup.EntityRemark
import uz.davrbank.compliance.backup.EntitySOE
import uz.davrbank.compliance.backup.EntitySanction
import uz.davrbank.compliance.backup.EntitySourceItem
import uz.davrbank.compliance.backup.Type
import java.time.LocalDate
import java.util.Date


data class CompliancePositionDataRiskResponse(
    var entityPositionGUID: String?, var position: String?, var lastUpdated: Date?
) {
    companion object {
        fun toDto(position: EntityPosition): CompliancePositionDataRiskResponse {
            return CompliancePositionDataRiskResponse(
                position.id, position.position, position.lastUpdated
            )
        }
    }
}

data class ComplianceAdditionalSegmentResponse(
    var id: Long,
    var type: String?,
    var records: List<ComplianceRecordItemResponse>
)

data class ComplianceRecordItemResponse(
    var id: Long,
    val derivedName: String?,
    val derivedValue: String?,
)

data class ComplianceCountryAssociationResponse(
    var id: String,
    var administrativeUnitName: String?,
    var associationTypeDesc: String?,
    var isostandard: String?,
    var lastUpdated: Date?,
    var ownershipPercentageCalc: String? = null,
) {
    companion object {
        fun toDto(entity: EntityCountryAssociation): ComplianceCountryAssociationResponse {
            return ComplianceCountryAssociationResponse(
                entity.id,
                entity.administrativeUnitName,
                entity.associationTypeDesc,
                entity.isostandard,
                entity.lastUpdated,
            )
        }
    }
}

data class ComplianceIdentificationResponse(
    var id: String,
    val administrativeUnitName: String?,
    val expiration: LocalDate?,
    val isoStandard: String?,
    val identificationIssuer: String?,
    val identificationNumber: String?,
    val identificationTypeDesc: String?,
    val issueDate: LocalDate?,
    val lastUpdated: Date?,
) {
    companion object {
        fun toDto(entity: EntityIdentification): ComplianceIdentificationResponse {
            return ComplianceIdentificationResponse(
                entity.id,
                entity.administrativeUnitName,
                entity.expiration,
                entity.isoStandard,
                entity.identificationIssuer,
                entity.identificationNumber,
                entity.identificationTypeDesc,
                entity.issueDate,
                entity.lastUpdated
            )
        }
    }
}

data class ComplianceAliasResponse(
    val aliasTypeDesc: String?,
    val englishDescription: String?,
    val entityAliasGuid: String?,
    val firstName: String?,
    val lastName: String?,
    val lastUpdated: Date?,
    val middleName: String?,
    val name: String?,
    val prefix: String?,
    val suffix: String?,
) {
    companion object {
        fun toDto(entityAlias: EntityAlias): ComplianceAliasResponse {
            return ComplianceAliasResponse(
                entityAlias.aliasTypeDesc,
                entityAlias.englishDescription,
                entityAlias.id,
                entityAlias.firstName,
                entityAlias.lastName,
                entityAlias.lastUpdated,
                entityAlias.middleName,
                entityAlias.name,
                entityAlias.prefix,
                entityAlias.suffix,
            )
        }
    }
}

data class ComplianceAddressResponse(
    val entityAddressGUID: String?,
    val addressTypeDesc: String?,
    val city: String?,
    val stateProvinceRegion: String?,
    val country: String?,
    val isoStandard: String?,
    val lastUpdated: Date?,
    val postalCode: String?,
    val address1: String?,
    val address2: String?,
) {
    companion object {
        fun toDto(entityAddress: EntityAddress): ComplianceAddressResponse {
            return ComplianceAddressResponse(
                entityAddress.id,
                entityAddress.addressTypeDesc,
                entityAddress.city,
                entityAddress.stateProvinceRegion,
                entityAddress.country,
                entityAddress.isoStandard,
                entityAddress.lastUpdated,
                entityAddress.postalCode,
                entityAddress.address1,
                entityAddress.address2,
            )
        }
    }
}


data class ComplianceRemarkResponse(
    val entityRemarkGUID: String?,
    val remark: String?,
    val lastUpdated: Date?,
) {
    companion object {
        fun toDto(remark: EntityRemark): ComplianceRemarkResponse {
            return ComplianceRemarkResponse(remark.id, remark.remark, remark.lastUpdated)
        }
    }
}

data class ComplianceSourceItemResponse(
    var entitySourceItemGUID: String?,
    var sourceURI: String?,
    var lastUpdated: Date?,
) {
    companion object {
        fun toDto(sourceItem: EntitySourceItem): ComplianceSourceItemResponse {
            return ComplianceSourceItemResponse(
                sourceItem.id, sourceItem.sourceURI, sourceItem.lastUpdated
            )
        }
    }
}


data class ComplianceDataRiskResponse(
    val fullName: String,
    val gender: String,
    val isDeceased: Boolean?,
    val id: String,
    val complianceType: ComplianceType,
    val positions: List<CompliancePositionDataRiskResponse>,
    var lastUpdated: Long,
    val aliases: List<ComplianceAliasResponse>,
    val addresses: List<ComplianceAddressResponse>,
    val remarks: List<ComplianceRemarkResponse>,
    val sourceItems: List<ComplianceSourceItemResponse>,
    val riskSegments: EntitySegmentAdminResponse,
    var relatedEntities: List<ComplianceRelatedEntityDataRiskResponse>? = null,
    var additionalSegments : List<ComplianceAdditionalSegmentResponse>,
    var countryAssociations: List<ComplianceCountryAssociationResponse>,
    var identifications: List<ComplianceIdentificationResponse>,
    var country: List<ComplianceCountryAssociationResponse>,
    val birthDate: LocalDate?,
    val type: Type
)

data class ComplianceResponse(
    val percentage: Double
)


data class EntitySegmentAdminResponse(
    var sanctions: List<SanctionsSegmentResponse> = emptyList(),
    var pep: List<PepSegmentResponse> = emptyList(),
    var adverseMedia: List<AdverseMediaSegmentResponse> = emptyList(),
    var soe: List<SoeSegmentResponse> = emptyList(),
    var enforcement: List<EnforcementSegmentResponse> = emptyList(),
)


data class AdverseMediaSegmentResponse(
    var id: String,
    var adverseMediaDesc: String?,
    var lastUpdated: Long?,
    var subCategory: List<AdverseMediaSubcategoryResponse>
) {
    companion object {
        fun toDto(entity: EntityAdverseMedia, subCategory: List<AdverseMediaSubcategoryResponse>) = entity.run {
            AdverseMediaSegmentResponse(
                id,
                adverseMediaDesc,
                lastUpdated?.time,
                subCategory
            )
        }
    }
}

data class AdverseMediaSubcategoryResponse(
    var id: String,
    var subCategoryLabel: String?,
    var lastUpdated: Long?,
)

data class PepSegmentResponse(
    var id: String,
    var isPrimaryPEP: Long?,
    var isActivePEP: String?,
    var isInCountryPEPOnly: String?,
    var pepAdminLevelDesc: String?,
    var isoAdministrativeUnitLevel0: String?,
    var administrativeUnitLevel0: String?,
    var governingInstitution: String?,
    var governingRole: String?,
    var lastUpdated: Long?,
    var effectiveDate: LocalDate?,
    var expirationDate: LocalDate?,
    var subCategory: List<PepSegmentSubCategoryResponse>,
) {
    companion object {
        fun toDto(entityPep: EntityPEP, subCategories: List<PepSegmentSubCategoryResponse>) = entityPep.run {
            PepSegmentResponse(
                id,
                isPrimaryPEP,
                isActivePEP,
                isInCountryPEPOnly,
                pepAdminLevelDesc,
                isoAdministrativeUnitLevel0,
                administrativeUnitLevel0,
                governingInstitution,
                governingRole,
                lastUpdated?.time,
                effectiveDate,
                expirationDate,
                subCategories
            )
        }
    }
}

data class PepSegmentSubCategoryResponse(
    var id: String,
    var pepSubcategoryDesc: String?,
    var subCategoryLabel: String?,
    var lastUpdated: Long?,
)

data class SoeSegmentResponse(
    var id: String,
    var isActive: Boolean,
    var subcategory: List<SoeSegmentSubCategoryResponse>,
    var domain: List<SoeSegmentDomainResponse>
) {
    companion object {
        fun toDto(
            entity: EntitySOE,
            subCategory: List<SoeSegmentSubCategoryResponse>,
            domain: List<SoeSegmentDomainResponse>
        ) = entity.run {
            SoeSegmentResponse(id, isActive, subCategory, domain)
        }
    }
}

data class SoeSegmentSubCategoryResponse(
    var id: String,
    var label: String?,
    var lastUpdated: Long?,
)


data class SoeSegmentDomainResponse(
    var id: String,
    val soeDomainDesc: String,
    val lastUpdated: Long,
)


data class SanctionsSegmentResponse(
    var id: String,
    var subCategoryLabel: String?,
    var sourceName: String?,
    var sourceNameAbbrev: String?,
    var administrativeUnitName: String?,
    var lastUpdated: Long,
) {
    companion object {
        fun toDto(entitySanction: EntitySanction) = entitySanction.run {
            SanctionsSegmentResponse(
                id,
                subCategoryLabel,
                sourceName,
                sourceNameAbbrev,
                administrativeUnitName,
                lastUpdated.time
            )
        }
    }
}

data class EnforcementSegmentResponse(
    var id: String,
    var enforcementDesc: String?,
    var sourceName: String?,
    var sourceNameAbbrev: String?,
    var administrativeUnitName: String?,
    var isoStandard: String?,
    var lastUpdated: Long?,
    var subCategory: List<EnforcementSegmentSubCategoryResponse>
) {
    companion object {
        fun toDto(enforcement: EntityEnforcement, subCategory: List<EnforcementSegmentSubCategoryResponse>) =
            enforcement.run {
                EnforcementSegmentResponse(
                    id,
                    enforcementDesc,
                    sourceName,
                    sourceNameAbbrev,
                    administrativeUnitName,
                    isoStandard,
                    lastUpdated?.time,
                    subCategory
                )
            }
    }
}

data class EnforcementSegmentSubCategoryResponse(
    var id: String,
    var subCategoryLabel: String,
    var lastUpdated: Long?,
)