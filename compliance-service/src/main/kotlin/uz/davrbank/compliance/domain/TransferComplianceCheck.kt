package uz.davrbank.compliance.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.PrePersist
import jakarta.persistence.Table
import org.hibernate.annotations.SQLDelete
import org.hibernate.annotations.SQLRestriction
import uz.davrbank.compliance.enums.GoodsType
import uz.davrbank.compliance.enums.TransferComplianceStatus
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "transfer_compliance_checks")
@SQLDelete(sql = "UPDATE transfer_compliance_checks SET deleted = true WHERE id = ?")
@SQLRestriction("deleted = false")
class TransferComplianceCheck(

    @Column(name = "check_number", nullable = false, unique = true, updatable = false, length = 64)
    var checkNumber: String,

    @Column(name = "external_id", nullable = false, unique = true, updatable = false)
    var externalId: UUID,

    @Column(name = "branch_code", nullable = false, length = 32)
    var branchCode: String,

    @Column(name = "client_name", nullable = false, columnDefinition = "TEXT")
    var clientName: String,

    @Column(name = "contract_type_code", nullable = false, length = 32)
    var contractTypeCode: String,

    @Column(name = "contract_number", nullable = false, length = 128)
    var contractNumber: String,

    @Column(name = "client_tin", length = 32)
    var clientTin: String? = null,

    @Column(name = "enterprise_registration_date")
    var enterpriseRegistrationDate: LocalDate? = null,

    @Column(name = "activity_type", columnDefinition = "TEXT")
    var activityType: String? = null,

    @Column(name = "contract_date")
    var contractDate: LocalDate? = null,

    @Column(name = "contract_idn", length = 128)
    var contractIdn: String? = null,

    @Column(name = "foreign_partner_name", columnDefinition = "TEXT")
    var foreignPartnerName: String? = null,

    @Column(name = "foreign_partner_tin", length = 64)
    var foreignPartnerTin: String? = null,

    @Column(name = "director_and_related_parties", columnDefinition = "TEXT")
    var directorAndRelatedParties: String? = null,

    @Column(name = "foreign_partner_country_code", length = 8)
    var foreignPartnerCountryCode: String? = null,

    @Column(name = "foreign_partner_bank", columnDefinition = "TEXT")
    var foreignPartnerBank: String? = null,

    @Column(name = "bic_swift", length = 32)
    var bicSwift: String? = null,

    @Column(name = "foreign_partner_bank_country_code", length = 8)
    var foreignPartnerBankCountryCode: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "goods_type", length = 16)
    var goodsType: GoodsType? = null,

    @Column(name = "goods_name", columnDefinition = "TEXT")
    var goodsName: String? = null,

    @Column(name = "goods_tnved_code", length = 64)
    var goodsTnvedCode: String? = null,

    @Column(name = "goods_origin_country_code", length = 8)
    var goodsOriginCountryCode: String? = null,

    @Column(name = "consignor", columnDefinition = "TEXT")
    var consignor: String? = null,

    @Column(name = "consignee", columnDefinition = "TEXT")
    var consignee: String? = null,

    @Column(name = "currency_code", length = 8)
    var currencyCode: String? = null,

    @Column(name = "operation_amount", precision = 20, scale = 2)
    var operationAmount: BigDecimal? = null,

    @Column(name = "last_operation_date")
    var lastOperationDate: LocalDate? = null,

    @Column(name = "fea_officer_full_name", length = 256)
    var feaOfficerFullName: String? = null,

    @Column(name = "decision_note", columnDefinition = "TEXT")
    var decisionNote: String? = null,

    @Column(name = "description", columnDefinition = "TEXT")
    var description: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    var status: TransferComplianceStatus = TransferComplianceStatus.NEW,

    @Column(name = "checked_by")
    var checkedBy: String? = null,

    @Column(name = "checked_date")
    var checkedDate: LocalDateTime? = null,

    @Column(name = "created_date", nullable = false, updatable = false)
    var createdDate: LocalDateTime? = null,

    @Column(name = "deleted", nullable = false)
    var deleted: Boolean = false,

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,
) {

    @PrePersist
    fun prePersist() {
        if (createdDate == null) {
            createdDate = LocalDateTime.now()
        }
    }
}
