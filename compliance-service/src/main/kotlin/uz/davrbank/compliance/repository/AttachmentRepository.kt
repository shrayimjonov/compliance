package uz.davrbank.compliance.repository

import org.springframework.data.jpa.repository.JpaRepository
import uz.davrbank.compliance.domain.Attachment
import uz.davrbank.compliance.enums.AttachmentEntityType
import uz.davrbank.compliance.enums.AttachmentUploadStatus
import java.util.UUID

interface AttachmentRepository : JpaRepository<Attachment, UUID> {

    fun findAllByIdInAndStatus(ids: Collection<UUID>, status: AttachmentUploadStatus): List<Attachment>

    fun findAllByEntityIdAndEntityTypeAndStatus(
        entityId: UUID,
        entityType: AttachmentEntityType,
        status: AttachmentUploadStatus,
    ): List<Attachment>

    fun findAllByEntityIdInAndEntityTypeAndStatus(
        entityIds: Collection<UUID>,
        entityType: AttachmentEntityType,
        status: AttachmentUploadStatus,
    ): List<Attachment>
}