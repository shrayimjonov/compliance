package uz.davrbank.compliance.services

import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uz.davrbank.compliance.*
import uz.davrbank.compliance.backup.*



@Service
class EntityRelatedDataProcessor(
    private val entityDOBRepository: EntityDOBRepository,
    private val entityAddressRepository: EntityAddressRepository,
    private val entityAliasRepository: EntityAliasRepository,
    private val entityIdentificationRepository: EntityIdentificationRepository,
    private val entityCountryAssociationRepository: EntityCountryAssociationRepository,
    private val entityPositionRepository: EntityPositionRepository,
    private val entityRelationshipRepository: EntityRelationshipRepository,
    private val entityRemarkRepository: EntityRemarkRepository,
    private val entitySourceItemRepository: EntitySourceItemRepository,
    private val entitySanctionRepository: EntitySanctionRepository
) {
    private val logger = KotlinLogging.logger {}

    @Transactional(readOnly = true)
    fun populateRelatedData(entity: Compliance, absDataCheckerItem: AbsDataCheckerItem) {
        absDataCheckerItem.apply {
            entityDOB = entityDOBRepository.findAllByCompliance(entity)
            entityAddress = entityAddressRepository.findAllByCompliance(entity)
            entityAlias = entityAliasRepository.findAllByCompliance(entity)
            entityIdentification = entityIdentificationRepository.findAllByCompliance(entity)
            entityCountryAssociation = entityCountryAssociationRepository.findAllByCompliance(entity)
            entityPosition = entityPositionRepository.findAllByCompliance(entity)
            entityRelationship = entityRelationshipRepository.findAllByCompliance(entity)
            entityRemark = entityRemarkRepository.findAllByCompliance(entity)
            entitySourceItem = entitySourceItemRepository.findAllByCompliance(entity)
            entitySanction = entitySanctionRepository.findAllByCompliance(entity)
        }
    }
}




interface EntitySegmentProcessorService {
    fun processSegments(entity: Compliance, absDataCheckerItem: AbsDataCheckerItem)
}



@Service
class EntitySegmentProcessorServiceImpl(
    private val entityPepRepository: EntityPepRepository,
    private val entityPepSubCategoryRepository: EntityPepSubCategoryRepository,
    private val entityAdverseMediaRepository: EntityAdverseMediaRepository,
    private val entityAdverseMediaSubCategoryRepository: EntityAdverseMediaSubCategoryRepository,
    private val entityEnforcementRepository: EntityEnforcementRepository,
    private val entityEnforcementSubcategoryRepository: EntityEnforcementSubcategoryRepository,
    private val entitySoeRepository: EntitySoeRepository,
    private val entitySoeSubCategoryRepository: EntitySOESubcategoryRepository,
    private val entitySOEDomainRepository: EntitySOEDomainRepository,
    private val additionalSegmentRepository: AdditionalSegmentRepository,
    private val recordRepository: RecordRepository,
    private val fieldRepository: FieldRepository,
    private val entitySegmentRepository: EntitySegmentRepository
): EntitySegmentProcessorService {
    private val logger = KotlinLogging.logger {}


    @Transactional(readOnly = true)
    override fun processSegments(entity: Compliance, absDataCheckerItem: AbsDataCheckerItem) {
        val segments = entitySegmentRepository.findAllByComplianceAndDeletedFalse(entity)

        segments.forEach { segment ->
            segment.apply {
                if (entitySOE) processSOEData(entity, absDataCheckerItem)
                if (additionalSegment) processAdditionalSegmentData(entity, absDataCheckerItem)
                if (entityPep) processPEPData(entity, absDataCheckerItem)
                if (entityEnforcement) processEnforcementData(entity, absDataCheckerItem)
                if (entityAdverseMedia) processAdverseMediaData(entity, absDataCheckerItem)
            }
        }
    }

    private fun processSOEData(entity: Compliance, absDataCheckerItem: AbsDataCheckerItem) {
        absDataCheckerItem.apply {
            entitySOE = entitySoeRepository.findAllByCompliance(entity)
            entitySOESubcategory = entitySoeSubCategoryRepository.findAllByCompliance(entity)
            entitySOEDomain = entitySOEDomainRepository.findAllByCompliance(entity)
        }
    }

    private fun processAdditionalSegmentData(entity: Compliance, absDataCheckerItem: AbsDataCheckerItem) {
        absDataCheckerItem.apply {
            additionalSegment = additionalSegmentRepository.findAllByComplianceAndDeletedFalse(entity)
            field = fieldRepository.findAllByComplianceAndDeletedFalse(entity)
            record = recordRepository.findAllByCompliance(entity)
        }
    }

    private fun processPEPData(entity: Compliance, absDataCheckerItem: AbsDataCheckerItem) {
        absDataCheckerItem.apply {
            entityPEP = entityPepRepository.findAllByCompliance(entity)
            entityPEPSubcategory = entityPepSubCategoryRepository.findAllByCompliance(entity)
        }
    }

    private fun processEnforcementData(entity: Compliance, absDataCheckerItem: AbsDataCheckerItem) {
        absDataCheckerItem.apply {
            entityEnforcement = entityEnforcementRepository.findAllByCompliance(entity)
            entityEnforcementSubcategory = entityEnforcementSubcategoryRepository.findAllByCompliance(entity)
        }
    }

    private fun processAdverseMediaData(entity: Compliance, absDataCheckerItem: AbsDataCheckerItem) {
        absDataCheckerItem.apply {
            entityAdverseMedia = entityAdverseMediaRepository.findAllByCompliance(entity)
            entityAdverseMediaSubcategory = entityAdverseMediaSubCategoryRepository.findAllByCompliance(entity)
        }
    }
}
