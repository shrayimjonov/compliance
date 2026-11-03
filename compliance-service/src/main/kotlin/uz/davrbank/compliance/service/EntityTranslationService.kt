package uz.davrbank.compliance.service

import uz.davrbank.compliance.dto.request.EntityTranslationRequest
import uz.davrbank.compliance.dto.response.EntityTranslationResponse

interface EntityTranslationService {
    fun translate(
        request: EntityTranslationRequest,
    ): EntityTranslationResponse
}
