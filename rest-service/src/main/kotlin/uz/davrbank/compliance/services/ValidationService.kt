package uz.davrbank.compliance.services


import org.springframework.stereotype.Component
import uz.davrbank.compliance.AbsDataCheckerRequest
import uz.davrbank.compliance.ValidationException
import uz.davrbank.compliance.backup.Type

@Component
class AbsValidationService {
    fun validateRequest(request: AbsDataCheckerRequest) {
        when (request.type) {
            Type.Individual -> validateIndividualRequest(request)
            Type.Organization -> validateOrganizationRequest(request)
            else -> throw ValidationException("Unsupported type: ${request.type}")
        }
    }

    private fun validateIndividualRequest(request: AbsDataCheckerRequest) {
        val missingFields = buildList {
            if (request.firstname.isNullOrBlank()) add("firstname")
            if (request.lastname.isNullOrBlank()) add("lastname")
            if (request.patronymic.isNullOrBlank()) add("patronymic")
            if (request.gender.isNullOrBlank()) add("gender")
            if (request.birthday == null) add("birthday")
            if (request.nationality.isNullOrBlank()) add("nationality")
            if (request.clientInn.isNullOrBlank()) add("clientInn")
            if (request.branch.isNullOrBlank()) add("branch")
        }

        if (missingFields.isNotEmpty()) {
            throw ValidationException(
                "Missing required fields for Individual: ${missingFields.joinToString(", ")}"
            )
        }
    }

    private fun validateOrganizationRequest(request: AbsDataCheckerRequest) {
        val missingFields = buildList {
            if (request.orgName.isNullOrBlank()) add("orgName")
            if (request.country.isNullOrBlank()) add("country")
            if (request.swiftCode.isNullOrBlank()) add("swiftCode")
            if (request.address.isNullOrBlank()) add("address")
            if (request.director.isNullOrBlank()) add("director")
            if (request.directorInn.isNullOrBlank()) add("directorInn")
            if (request.founder.isNullOrBlank()) add("founder")
            if (request.founderInn.isNullOrBlank()) add("founderInn")
            if (request.orgRegistrationDate == null) add("orgRegistrationDate")
            if (request.participationShare == null) add("participationShare")
            if (request.branch.isNullOrBlank()) add("branch")
            if (request.actionType.isNullOrBlank()) add("actionType")
            if (request.requestedDate == null) add("requestedDate")
        }

        if (missingFields.isNotEmpty()) {
            throw ValidationException(
                "Missing required fields for Organization: ${missingFields.joinToString(", ")}"
            )
        }
    }
}
