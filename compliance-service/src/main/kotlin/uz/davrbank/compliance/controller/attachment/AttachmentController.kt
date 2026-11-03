package uz.davrbank.compliance.controller.attachment

import org.springframework.core.io.ByteArrayResource
import org.springframework.core.io.Resource
import org.springframework.http.ContentDisposition
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import uz.davrbank.compliance.constants.ApiConstants
import uz.davrbank.compliance.dto.response.FileDetailDTO
import uz.davrbank.compliance.dto.response.ResponseDTO
import uz.davrbank.compliance.service.AttachmentService
import java.util.UUID

@Validated
@RestController
@RequestMapping(ApiConstants.ATTACHMENT_ROOT_API)
class AttachmentController(
    private val attachmentService: AttachmentService,
) {

    @PostMapping(ApiConstants.UPLOAD, consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun upload(
        @RequestPart("files") files: List<MultipartFile>,
    ): ResponseEntity<ResponseDTO<List<FileDetailDTO>>> = ResponseEntity.ok(
        ResponseDTO.ok(attachmentService.upload(files))
    )

    @GetMapping(ApiConstants.DOWNLOAD + ApiConstants.ID)
    fun download(
        @PathVariable id: UUID,
    ): ResponseEntity<Resource> {
        val file = attachmentService.download(id)
        val contentDisposition = ContentDisposition.attachment()
            .filename(file.fileName)
            .build()

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString())
            .contentType(MediaType.parseMediaType(file.contentType))
            .contentLength(file.content.size.toLong())
            .body(ByteArrayResource(file.content))
    }

    @DeleteMapping
    fun remove(
        @RequestBody attachmentIds: List<UUID>,
    ): ResponseEntity<ResponseDTO<Unit>> {
        attachmentService.removeByIds(attachmentIds)
        return ResponseEntity.ok(ResponseDTO.ok(Unit))
    }
}
