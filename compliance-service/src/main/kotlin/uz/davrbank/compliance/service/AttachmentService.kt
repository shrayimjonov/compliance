package uz.davrbank.compliance.service

import uz.davrbank.compliance.dto.response.FileDetailDTO
import uz.davrbank.compliance.enums.AttachmentEntityType
import org.springframework.web.multipart.MultipartFile
import java.util.UUID

interface AttachmentService {

    fun upload(files: List<MultipartFile>): List<FileDetailDTO>

    fun download(id: UUID): DownloadedFile

    fun bindToEntity(attachmentIds: List<UUID>, entityId: UUID, entityType: AttachmentEntityType)

    fun getByEntity(entityId: UUID, entityType: AttachmentEntityType): List<FileDetailDTO>

    fun getByEntities(entityIds: Collection<UUID>, entityType: AttachmentEntityType): Map<UUID, List<FileDetailDTO>>

    fun removeByIds(attachmentIds: List<UUID>)

    fun syncEntityAttachments(entityId: UUID, entityType: AttachmentEntityType, requestFileIds: List<UUID>?)
}

data class DownloadedFile(
    val content: ByteArray,
    val fileName: String,
    val contentType: String,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DownloadedFile

        if (!content.contentEquals(other.content)) return false
        if (fileName != other.fileName) return false
        if (contentType != other.contentType) return false

        return true
    }

    override fun hashCode(): Int {
        var result = content.contentHashCode()
        result = 31 * result + fileName.hashCode()
        result = 31 * result + contentType.hashCode()
        return result
    }
}
