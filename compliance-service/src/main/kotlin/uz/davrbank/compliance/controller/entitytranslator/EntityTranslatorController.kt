package uz.davrbank.compliance.controller.entitytranslator

import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uz.davrbank.compliance.constants.ApiConstants
import uz.davrbank.compliance.dto.request.EntityTranslationRequest
import uz.davrbank.compliance.dto.response.EntityTranslationResponse
import uz.davrbank.compliance.dto.response.ResponseDTO
import uz.davrbank.compliance.service.EntityTranslationService

@Validated
@RestController
@RequestMapping(ApiConstants.ENTITY_TRANSLATOR_ROOT_API)
class EntityTranslatorController(
    private val entityTranslationService: EntityTranslationService,
) {

    @PostMapping(ApiConstants.TRANSLATE)
    fun translate(
        @Valid @RequestBody request: EntityTranslationRequest,
    ): ResponseEntity<ResponseDTO<EntityTranslationResponse>> = ResponseEntity.ok(
        ResponseDTO.ok(entityTranslationService.translate(request))
    )
}
