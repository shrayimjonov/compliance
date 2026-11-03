package uz.davrbank.compliance

enum class ComplianceType {
    ENTITY,
    RELATIONSHIP
}

// Bu "Entity" qaysi risk toifalariga (sanktsiya, PEP, OAV va h.k.) kirishini ko'rsatuvchi ma'lumotlar
enum class ComplianceSegment(val info: String) {
    PEP("Siyosiy ahamiyatga ega shaxs"),
    SANCTION("Rasmiy sanktsiyalar ro'yxatlariga kiritilganligi haqida ma'lumot"),
    ADVERSE_MEDIA("Salbiy OAVda chiqqan ma'lumotlar"),
    ENFORCEMENT("Huquqni muhofaza qilish organlari tomonidan qo'llanilgan choralar"),
    SOE("Davlatga tegishli korxona"),
    DEFAULT("DEFAULT")
}

enum class UserRole {
    DEV, ADMIN
}

enum class ErrorCode(val code: Long) {
    GENERAL_API_EXCEPTION(100),
    USER_NOT_FOUND_EXCEPTION(101),
    UNSUPPORTED_GRANT_TYPE(102)
}

enum class DataType {
    LEXIS,
    BANK
}

enum class AbsHandlerStatus {
    CREATED,
    FINISHED,
    FAILED,
    UPDATED
}

enum class ComplianceStatus {
    REJECTED,
    APPROVED,
    NOT_CHECKED,
    UPDATED
}

enum class AbsCheckOperationType {
    OPEN_ACCOUNT,
    SWIFT_TRANSACTION_IN,
    SWIFT_TRANSACTION_OUT,
    TRANSACTION_OUT,
    TRANSACTION_IN,
}

enum class FileType {
    DELTA,
    FULL
}


enum class Status {
    COMPLETED, //
    UPLOAD, // AGAR STATUS UPLOAD BO'LSA SCHEDULER UPLOADEDLARNI KAFKAGA OTADI
    DOWNLOAD,  // ESA  YUKLANGAN

}