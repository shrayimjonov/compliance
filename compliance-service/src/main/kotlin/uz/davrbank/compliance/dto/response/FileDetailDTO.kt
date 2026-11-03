package uz.davrbank.compliance.dto.response

import uz.davrbank.compliance.domain.Attachment

data class FileDetailDTO(
    val id: String,
    val url: String,
    val name: String? = null,
)

internal fun Attachment.toFileDetailDTO(): FileDetailDTO = FileDetailDTO(
    id = requireNotNull(id) { "Attachment id must not be null." }.toString(),
    url = attachmentUrl,
    name = originalName,
)
