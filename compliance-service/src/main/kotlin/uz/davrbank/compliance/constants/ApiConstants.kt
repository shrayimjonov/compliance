package uz.davrbank.compliance.constants

class ApiConstants private constructor() {
    companion object {
        const val API = "/api"
        const val VERSION = "/v1"
        const val SERVICE_NAME = "/compliance"

        const val DICTIONARY = "/dictionary"
        const val ELASTICSEARCH = "/es"
        const val DETAILS = "/details"
        const val LOCAL = "/local"
        const val ABS_CHECKER = "/abs-checker"
        const val API_LOG = "/api-log"
        const val SETTINGS = "/settings"
        const val ENTITY_TRANSLATOR = "/entity-translator"
        const val ATTACHMENT = "/attachment"
        const val UPLOAD = "/upload"
        const val DOWNLOAD = "/download"
        const val TRANSFER_COMPLIANCE_CHECK = "/transfer-compliance-check"
        const val REPORT = "/report"
        const val ELASTICSEARCH_RANK_RANGE = "/elasticsearch/rank-range"
        const val LIST = "/list"
        const val EXTERNAL = "/external"
        const val SEARCH = "/search"
        const val TRANSLATE = "/translate"
        const val COUNTRIES = "/countries"
        const val NATIONALITIES = "/nationalities"
        const val BRANCHES = "/branches"
        const val CONTRACT_TYPES = "/contract-types"
        const val CURRENCIES = "/currencies"
        const val ENUMS = "/enums"
        const val CHECK_ACCOUNT = "/check-account"
        const val CHECK_STATUS = "/check-status"
        const val CHANGE_STATUS = "/change-status"
        const val RESPONSE = "/response"
        const val STATUS = "/status"

        const val ID = "/{id}"
        const val EXTERNAL_ID = "/{externalId}"

        const val ROOT_API = API + SERVICE_NAME + VERSION
        const val DICTIONARY_ROOT_API = ROOT_API + DICTIONARY
        const val ELASTICSEARCH_ROOT_API = ROOT_API + ELASTICSEARCH
        const val COMPLIANCE_ROOT_API = ROOT_API + DETAILS
        const val LOCAL_COMPLIANCE_ROOT_API = ROOT_API + LOCAL
        const val ABS_CHECKER_ROOT_API = ROOT_API + ABS_CHECKER
        const val API_LOG_ROOT_API = ROOT_API + API_LOG
        const val SETTINGS_ROOT_API = ROOT_API + SETTINGS
        const val ENTITY_TRANSLATOR_ROOT_API = ROOT_API + ENTITY_TRANSLATOR
        const val ATTACHMENT_ROOT_API = ROOT_API + ATTACHMENT
        const val TRANSFER_COMPLIANCE_CHECK_ROOT_API = ROOT_API + TRANSFER_COMPLIANCE_CHECK
    }
}
