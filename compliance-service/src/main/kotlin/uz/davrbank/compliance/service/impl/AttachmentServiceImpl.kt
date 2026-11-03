package uz.davrbank.compliance.service.impl

import io.minio.BucketExistsArgs
import io.minio.GetObjectArgs
import io.minio.MakeBucketArgs
import io.minio.MinioClient
import io.minio.PutObjectArgs
import io.minio.RemoveObjectArgs
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import uz.davrbank.compliance.config.ApplicationProperties
import uz.davrbank.compliance.domain.Attachment
import uz.davrbank.compliance.dto.response.FileDetailDTO
import uz.davrbank.compliance.dto.response.toFileDetailDTO
import uz.davrbank.compliance.enums.AttachmentContentType
import uz.davrbank.compliance.enums.AttachmentEntityType
import uz.davrbank.compliance.enums.AttachmentUploadStatus
import uz.davrbank.compliance.exception.NotFoundException
import uz.davrbank.compliance.repository.AttachmentRepository
import uz.davrbank.compliance.service.AttachmentService
import uz.davrbank.compliance.service.DownloadedFile
import java.util.Locale
import java.util.UUID

@Service
class AttachmentServiceImpl(
    private val attachmentRepository: AttachmentRepository,
    private val minioClient: MinioClient?,
    private val applicationProperties: ApplicationProperties,
) : AttachmentService {

    private fun requireMinioClient(): MinioClient =
        requireNotNull(minioClient) { "MinIO is not configured. Set application.min-io.url to enable file operations." }

    @Transactional
    override fun upload(files: List<MultipartFile>): List<FileDetailDTO> {
        require(files.isNotEmpty()) { "Files must not be empty" }
        val minIo = applicationProperties.minIo
        val bucket = minIo.bucket.also { require(it.isNotBlank()) { "MinIO bucket is not configured" } }

        return files.map { file ->
            require(!file.isEmpty) { "File must not be empty" }
            val attachment = createAttachment(file, bucket)
            attachmentRepository.save(attachment)
            uploadToMinio(file, attachment)
            attachment.status = AttachmentUploadStatus.COMPLETED
            attachmentRepository.save(attachment)
            attachment.toFileDetailDTO()
        }
    }

    @Transactional(readOnly = true)
    override fun download(id: UUID): DownloadedFile {
        val attachment = attachmentRepository.findByIdOrNull(id)
            ?: throw NotFoundException("Attachment not found for id=$id")

        val content = try {
            requireMinioClient().getObject(
                GetObjectArgs.builder()
                    .bucket(attachment.bucketName)
                    .`object`(attachment.objectName)
                    .build()
            ).use { it.readBytes() }
        } catch (ex: Exception) {
            throw RuntimeException("Failed to download attachment id=$id from storage", ex)
        }

        return DownloadedFile(
            content = content,
            fileName = attachment.originalName ?: attachment.objectName.substringAfterLast('/'),
            contentType = attachment.contentType.toMimeType(),
        )
    }

    @Transactional
    override fun bindToEntity(attachmentIds: List<UUID>, entityId: UUID, entityType: AttachmentEntityType) {
        if (attachmentIds.isEmpty()) return
        val attachments = attachmentRepository.findAllByIdInAndStatus(attachmentIds, AttachmentUploadStatus.COMPLETED)
        attachments.forEach { attachment ->
            attachment.entityId = entityId
            attachment.entityType = entityType
        }
        attachmentRepository.saveAll(attachments)
    }

    @Transactional(readOnly = true)
    override fun getByEntity(entityId: UUID, entityType: AttachmentEntityType): List<FileDetailDTO> =
        attachmentRepository
            .findAllByEntityIdAndEntityTypeAndStatus(entityId, entityType, AttachmentUploadStatus.COMPLETED)
            .map(Attachment::toFileDetailDTO)

    @Transactional(readOnly = true)
    override fun getByEntities(
        entityIds: Collection<UUID>,
        entityType: AttachmentEntityType,
    ): Map<UUID, List<FileDetailDTO>> {
        if (entityIds.isEmpty()) return emptyMap()
        return attachmentRepository
            .findAllByEntityIdInAndEntityTypeAndStatus(entityIds, entityType, AttachmentUploadStatus.COMPLETED)
            .groupBy { requireNotNull(it.entityId) }
            .mapValues { (_, attachments) -> attachments.map(Attachment::toFileDetailDTO) }
    }

    @Transactional
    override fun removeByIds(attachmentIds: List<UUID>) {
        if (attachmentIds.isEmpty()) return
        val attachments = attachmentRepository.findAllById(attachmentIds)
        attachments.forEach { attachment ->
            runCatching {
                requireMinioClient().removeObject(
                    RemoveObjectArgs.builder()
                        .bucket(attachment.bucketName)
                        .`object`(attachment.objectName)
                        .build()
                )
            }
        }
        attachmentRepository.deleteAll(attachments)
    }

    @Transactional
    override fun syncEntityAttachments(entityId: UUID, entityType: AttachmentEntityType, requestFileIds: List<UUID>?) {
        if (requestFileIds == null) return

        val existing = attachmentRepository
            .findAllByEntityIdAndEntityTypeAndStatus(entityId, entityType, AttachmentUploadStatus.COMPLETED)

        val requestIdSet = requestFileIds.toSet()
        val toRemove = existing.filter { it.id != null && it.id !in requestIdSet }

        if (toRemove.isNotEmpty()) {
            removeByIds(toRemove.mapNotNull { it.id })
        }

        val existingIdSet = existing.mapNotNull { it.id }.toSet()
        val toAdd = requestFileIds.filter { it !in existingIdSet }

        if (toAdd.isNotEmpty()) {
            bindToEntity(toAdd, entityId, entityType)
        }
    }

    private fun createAttachment(file: MultipartFile, bucket: String): Attachment {
        val contentType = resolveContentType(file)
        val extension = resolveExtension(file)
        val objectName = "${FOLDER}/${UUID.randomUUID()}$extension"
        val originalName = file.originalFilename?.let(org.springframework.util.StringUtils::cleanPath)

        return Attachment(
            bucketName = bucket,
            objectName = objectName,
            originalName = originalName,
            attachmentUrl = buildUrl(bucket, objectName),
            contentType = contentType,
        )
    }

    private fun uploadToMinio(file: MultipartFile, attachment: Attachment) {
        try {
            file.inputStream.use { inputStream ->
                ensureBucketExists(attachment.bucketName)
                requireMinioClient().putObject(
                    PutObjectArgs.builder()
                        .bucket(attachment.bucketName)
                        .`object`(attachment.objectName)
                        .stream(inputStream, file.size, -1)
                        .contentType(file.contentType ?: DEFAULT_CONTENT_TYPE)
                        .build()
                )
            }
        } catch (e: Exception) {
            runCatching {
                attachment.status = AttachmentUploadStatus.FAILED
                attachmentRepository.save(attachment)
            }
            throw RuntimeException("Attachment upload failed", e)
        }
    }

    private fun ensureBucketExists(bucket: String) {
        val client = requireMinioClient()
        val exists = client.bucketExists(BucketExistsArgs.builder().bucket(bucket).build())
        if (!exists) {
            runCatching {
                client.makeBucket(MakeBucketArgs.builder().bucket(bucket).build())
            }
        }
    }

    private fun buildUrl(bucket: String, objectName: String): String {
        val baseUrl = applicationProperties.minIo.url.trimEnd('/')
        return "$baseUrl/$bucket/$objectName"
    }

    private fun resolveContentType(file: MultipartFile): AttachmentContentType {
        val ct = file.contentType?.lowercase(Locale.ROOT) ?: ""
        return when {
            ct.contains("jpeg") || ct.contains("jpg") -> AttachmentContentType.IMAGE_JPEG
            ct.contains("png") -> AttachmentContentType.IMAGE_PNG
            ct.contains("webp") -> AttachmentContentType.IMAGE_WEBP
            ct.contains("gif") -> AttachmentContentType.IMAGE_GIF
            ct.contains("pdf") -> AttachmentContentType.PDF
            ct.contains("msword") -> AttachmentContentType.DOC
            ct.contains("wordprocessingml") -> AttachmentContentType.DOCX
            ct.contains("ms-excel") -> AttachmentContentType.XLS
            ct.contains("spreadsheetml") -> AttachmentContentType.XLSX
            ct.startsWith("text/") -> AttachmentContentType.TXT
            else -> resolveByExtension(file)
        }
    }

    private fun resolveByExtension(file: MultipartFile): AttachmentContentType {
        val ext = extractExtension(file.originalFilename)
        return when (ext) {
            "jpg", "jpeg" -> AttachmentContentType.IMAGE_JPEG
            "png" -> AttachmentContentType.IMAGE_PNG
            "webp" -> AttachmentContentType.IMAGE_WEBP
            "gif" -> AttachmentContentType.IMAGE_GIF
            "pdf" -> AttachmentContentType.PDF
            "doc" -> AttachmentContentType.DOC
            "docx" -> AttachmentContentType.DOCX
            "xls" -> AttachmentContentType.XLS
            "xlsx" -> AttachmentContentType.XLSX
            "txt" -> AttachmentContentType.TXT
            else -> AttachmentContentType.OTHER
        }
    }

    private fun resolveExtension(file: MultipartFile): String {
        val ext = extractExtension(file.originalFilename)
        return if (ext != null) ".$ext" else ""
    }

    private fun extractExtension(name: String?): String? =
        org.springframework.util.StringUtils.getFilenameExtension(name)?.lowercase(Locale.ROOT)

    private fun AttachmentContentType.toMimeType(): String = when (this) {
        AttachmentContentType.IMAGE_JPEG -> "image/jpeg"
        AttachmentContentType.IMAGE_PNG -> "image/png"
        AttachmentContentType.IMAGE_WEBP -> "image/webp"
        AttachmentContentType.IMAGE_GIF -> "image/gif"
        AttachmentContentType.PDF -> "application/pdf"
        AttachmentContentType.DOC -> "application/msword"
        AttachmentContentType.DOCX -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
        AttachmentContentType.XLS -> "application/vnd.ms-excel"
        AttachmentContentType.XLSX -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        AttachmentContentType.TXT -> "text/plain"
        AttachmentContentType.OTHER -> DEFAULT_CONTENT_TYPE
    }

    companion object {
        private const val FOLDER = "compliance"
        private const val DEFAULT_CONTENT_TYPE = "application/octet-stream"
    }
}
