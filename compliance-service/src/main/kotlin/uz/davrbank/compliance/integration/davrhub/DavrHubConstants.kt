package uz.davrbank.compliance.integration.davrhub

object DavrHubConstants {
    const val TOKEN_URI = "/api/v1/auth/oauth2/token"
    const val LEXIS_EXCEPTION_URI = "/api/v1/davrhub/custom/lexis-exception"

    const val SERVICE_NAME_HEADER = "X-Service-Name"
    const val DEFAULT_TOKEN_TYPE = "Bearer"

    const val WEB_CLIENT_BEAN = "davrHubWebClient"
}