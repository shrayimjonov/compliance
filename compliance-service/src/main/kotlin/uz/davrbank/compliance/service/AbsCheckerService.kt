package uz.davrbank.compliance.service

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import uz.davrbank.compliance.dto.request.abs.AbsCheckerChangeStatusRequest
import uz.davrbank.compliance.dto.request.abs.AbsCheckerRequest
import uz.davrbank.compliance.dto.request.filter.AbsCheckerListFilter
import uz.davrbank.compliance.dto.request.filter.BaseFilter
import uz.davrbank.compliance.dto.response.abs.*

interface AbsCheckerService {
    fun getList(filter: BaseFilter<AbsCheckerListFilter>): Page<AbsCheckerAdminResponse>

    fun get(id: String): AbsCheckerAdminGetOneResponse

    fun getResponse(id: String, pageable: Pageable): AbsCheckerSearchResponse?

    fun checkStatus(id: String): AbsCheckerAdminGetOneResponse

    fun updateStatus(id: String, request: AbsCheckerChangeStatusRequest): AbsCheckerAdminResponse

    fun getLogs(pageable: Pageable): Page<AbsApiLogResponse>

    fun create(request: AbsCheckerRequest): AbsCheckerResponse
}
