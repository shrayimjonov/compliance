package uz.davrbank.compliance.integration.davrhub

import org.springframework.http.HttpMethod
import org.springframework.stereotype.Service

@Service
class DavrHubLexisService(
    private val davrHubClient: DavrHubClient,
) {

    fun markAsChecked(request: LexisRequest): LexisResponse =
        davrHubClient.exchange(
            method = HttpMethod.POST,
            uri = DavrHubConstants.LEXIS_EXCEPTION_URI,
            requestBody = request,
            responseType = LexisResponse::class.java,
        )
            ?.takeIf { response -> response.lexId.isNotBlank() }
            ?: throw DavrHubCommunicationException("DavrHub Lexis exception response was empty")
}