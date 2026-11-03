package uz.davrbank.compliance.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.transaction.annotation.Transactional
import uz.davrbank.compliance.domain.TransferComplianceCheck
import uz.davrbank.compliance.enums.TransferComplianceStatus
import java.util.*

interface TransferComplianceCheckRepository :
    JpaRepository<TransferComplianceCheck, UUID>,
    JpaSpecificationExecutor<TransferComplianceCheck> {

    @Query(value = "SELECT nextval('transfer_compliance_check_number_seq')", nativeQuery = true)
    fun nextCheckNumberSequence(): Long

    fun findByExternalId(externalId: UUID): TransferComplianceCheck?

    @Transactional
    @Modifying
    @Query(
        "UPDATE TransferComplianceCheck t SET t.status = :newStatus " +
                "WHERE t.id = :id AND t.status = :expectedStatus"
    )
    fun updateStatusIfCurrent(
        @Param("id") id: UUID,
        @Param("expectedStatus") expectedStatus: TransferComplianceStatus,
        @Param("newStatus") newStatus: TransferComplianceStatus,
    ): Int
}
