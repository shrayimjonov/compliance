package uz.davrbank.compliance.services



import org.slf4j.LoggerFactory
import org.springframework.data.domain.PageRequest
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import uz.davrbank.compliance.DataType
import uz.davrbank.compliance.EsDataUpdater
import uz.davrbank.compliance.EsDataUpdaterRepository
import uz.davrbank.compliance.backup.ComplianceEntityPRepository
import uz.davrbank.compliance.backup.EntityCountryAssociationRepository
import uz.davrbank.compliance.backup.EntityDOBRepository

import uz.davrbank.compliance.elasticsearch.NewComplianceData
import uz.davrbank.compliance.elasticsearch.ComplianceDataRepository
import uz.davrbank.compliance.localDateToString



// yangi indexga malumotlarni qayta yozish uchun


@Component
class EsSaveScheduler(
    private val complianceDataRepository: ComplianceDataRepository,
    private val complianceEntityPRepository: ComplianceEntityPRepository,
    private val entityCountryAssociationRepository: EntityCountryAssociationRepository,
    private val entityDOBRepository: EntityDOBRepository,
    private val esDataUpdaterRepository: EsDataUpdaterRepository

) {
    private val log = LoggerFactory.getLogger(javaClass)


    @Scheduled(initialDelay = 60000, fixedDelay = 60000)
    fun saveData() {
        val esDataUpdater = esDataUpdaterRepository.findAll().firstOrNull() ?: run {
            esDataUpdaterRepository.save(EsDataUpdater(
                isActive = false
            ))
        }

        if (esDataUpdater.isActive) {
            return
        }



        log.info("Ma'lumotlarni migratsiya qilish boshlandi...")

        val pageSize = 1000
        var pageNumber = 0
        var totalSaved = 0

        do {
            val pageRequest = PageRequest.of(pageNumber, pageSize)
            val startTime = System.currentTimeMillis()
            val compliancePage = complianceEntityPRepository.findAllOrderByCreatedDate(pageRequest)
            log.info("${pageRequest} malumotlarini olish uchun ${System.currentTimeMillis() - startTime}ms")

            if (compliancePage.isEmpty) {
                log.info("Yangi ma'lumotlar topilmadi.")
                break
            }

            log.info("Sahifa {} ishlanmoqda, elementlar soni: {}", pageNumber, compliancePage.numberOfElements)

            val newComplianceEntityDocs = compliancePage.content.map { compliance ->

                val birthDate = entityDOBRepository.findAllByCompliance(compliance)
                    .map { it.birthDate }
                    .firstOrNull()?.let {
                        localDateToString(it)
                    }




                val country = entityCountryAssociationRepository.findAllByCompliance(compliance)
                    .filter { it.associationTypeDesc == "Citizenship" || it.associationTypeDesc == "Nationality" }
                    .associateBy { it.isostandard }
                    .values.toList()
                    .firstOrNull()?.administrativeUnitName ?: ""

                NewComplianceData(
                    firstname = compliance.firstName?.lowercase() ?: "",
                    lastname = compliance.lastName.lowercase() ?: "",
                    fullName = compliance.fullName.lowercase(),
                    extLookUpId = compliance.lookupId ?: "",
                    type = compliance.type.name,
                    complianceType = compliance.complianceType,
                    id = compliance.id,
                    dataType = DataType.LEXIS,
                    birth = birthDate,
                    region = country.lowercase()
                )
            }


            complianceDataRepository.saveAll(newComplianceEntityDocs)

            totalSaved += newComplianceEntityDocs.size
            log.info("Sahifa {} muvaffaqiyatli saqlandi. Jami saqlanganlar: {}", pageNumber, totalSaved)

            pageNumber++
            log.info("SPEND TIME1: ${System.currentTimeMillis() - startTime}ms")

        } while (compliancePage.hasNext())

        esDataUpdater.isActive = true
        esDataUpdaterRepository.save(esDataUpdater)
        log.info("Barcha ma'lumotlar muvaffaqiyatli ko'chirildi. Jami: {}", totalSaved)

    }
}