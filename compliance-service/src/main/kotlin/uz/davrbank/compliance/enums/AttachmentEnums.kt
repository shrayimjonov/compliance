package uz.davrbank.compliance.enums

enum class AttachmentUploadStatus {
    UPLOADING,
    COMPLETED,
    FAILED,
}

enum class AttachmentContentType {
    IMAGE_JPEG,
    IMAGE_PNG,
    IMAGE_WEBP,
    IMAGE_GIF,
    PDF,
    DOC,
    DOCX,
    XLS,
    XLSX,
    TXT,
    OTHER,
}

enum class AttachmentEntityType {
    TRANSFER_COMPLIANCE_CHECK,
}
