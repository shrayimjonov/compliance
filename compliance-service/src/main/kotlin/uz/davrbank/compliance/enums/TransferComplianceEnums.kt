package uz.davrbank.compliance.enums

enum class TransferComplianceStatus {
    NEW,
    SENT,
    CHECKED,
    APPROVED,
    REJECTED,
    CHECK_ERROR;

    companion object {
        /**
         * Allowed transitions: NEW -> SENT -> CHECKED -> APPROVED/REJECTED.
         * SENT -> CHECK_ERROR on background failure; CHECK_ERROR -> SENT to recheck.
         */
        private val TRANSITIONS: Map<TransferComplianceStatus, Set<TransferComplianceStatus>> = mapOf(
            NEW to setOf(SENT),
            SENT to setOf(CHECKED),
            CHECKED to setOf(APPROVED, REJECTED),
            CHECK_ERROR to setOf(SENT),
        )

        fun canTransition(from: TransferComplianceStatus, to: TransferComplianceStatus): Boolean =
            TRANSITIONS[from]?.contains(to) == true
    }
}

enum class TransferType {
    INCOMING,
    OUTGOING,
}

enum class GoodsType {
    PRODUCT,
    SERVICE,
}
