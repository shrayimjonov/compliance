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
import uz.davrbank.compliance.enums.AttachmentContentType
import uz.davrbank.compliance.enums.AttachmentEntityType
import uz.davrbank.compliance.enums.AttachmentUploadStatus
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "attachment")
class Attachment(

    @Column(name = "bucket_name", nullable = false)
    var bucketName: String,

    @Column(name = "object_name", nullable = false)
    var objectName: String,

    @Column(name = "original_name")
    var originalName: String? = null,

    @Column(name = "attachment_url", nullable = false)
    var attachmentUrl: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "content_type", nullable = false, length = 32)
    var contentType: AttachmentContentType = AttachmentContentType.OTHER,

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 16)
    var status: AttachmentUploadStatus = AttachmentUploadStatus.UPLOADING,

    @Column(name = "entity_id")
    var entityId: UUID? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "entity_type", length = 64)
    var entityType: AttachmentEntityType? = null,

    @Column(name = "created_date", nullable = false, updatable = false)
    var createdDate: LocalDateTime? = null,

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
