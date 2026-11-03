package uz.davrbank.compliance.enums

enum class AbsHandlerStatus {
    CREATED,
    FINISHED,
    FAILED,
    UPDATED,
}

enum class AbsComplianceStatus {
    REJECTED,
    APPROVED,
    NOT_CHECKED,
    UPDATED,
}

enum class AbsCheckOperationType {
    OPEN_ACCOUNT,
    SWIFT_TRANSACTION_IN,
    SWIFT_TRANSACTION_OUT,
    TRANSACTION_OUT,
    TRANSACTION_IN,
}

enum class AbsRequestType {
    Organization,
    Individual,
    Vessel,
    Country,
    Aircraft,
    Unknown,
}
