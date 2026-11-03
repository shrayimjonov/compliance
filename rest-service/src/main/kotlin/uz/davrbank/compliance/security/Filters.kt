package uz.davrbank.compliance.security

import jakarta.servlet.FilterChain
import jakarta.servlet.ReadListener
import jakarta.servlet.ServletInputStream
import jakarta.servlet.ServletOutputStream
import jakarta.servlet.WriteListener
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletRequestWrapper
import jakarta.servlet.http.HttpServletResponse
import jakarta.servlet.http.HttpServletResponseWrapper
import mu.KotlinLogging
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.filter.OncePerRequestFilter
import uz.davrbank.compliance.ApiLog
import uz.davrbank.compliance.ApiLogRepository
import uz.davrbank.compliance.queryStringToMap
import java.io.BufferedReader
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.io.PrintWriter
import org.springframework.stereotype.Component




class BearerTokenSecurityFilter(
    private val authenticationManager: AuthenticationManager,
    private val apiLogRepository: ApiLogRepository
) : OncePerRequestFilter() {
    private val logger = KotlinLogging.logger {}

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {

        val wrappedRequest = CachedBodyHttpServletRequest(request)
        val wrappedResponseWrapper = CachedBodyHttpServletResponse(response)

        val apiLog =
            ApiLog(
                header = wrappedRequest.headerNames.asSequence().joinToString { "$it: ${wrappedRequest.getHeader(it)}" },
                method = wrappedRequest.method,
                uri = wrappedRequest.requestURI,
                encoding = wrappedRequest.characterEncoding,
                urlParams = wrappedRequest.queryString?.let{ queryStringToMap(it) } ?: emptyMap(),
                requestBody = String(wrappedRequest.inputStream.readBytes(), Charsets.UTF_8)
            )

        try {
            val token = getBearerToken(request)
            if (token == null) {
                filterChain.doFilter(wrappedRequest, wrappedResponseWrapper)
                return
            }
            val usernamePasswordAuthenticationToken = UsernamePasswordAuthenticationToken.unauthenticated(null, token)
            val authentication = authenticationManager.authenticate(usernamePasswordAuthenticationToken)
            setSecurityContext(authentication)
            filterChain.doFilter(wrappedRequest, wrappedResponseWrapper)
        } finally {
            wrappedResponseWrapper.copyBodyToResponse()
            val customResponse = wrappedResponseWrapper.customOutputStream.toString(Charsets.UTF_8)
            apiLogRepository.save(apiLog.apply {
                this.responseBody = customResponse
            })
        }

    }

    private fun setSecurityContext(authentication: Authentication) {
        if (!authentication.isAuthenticated) return
        val securityContextHolderStrategy = SecurityContextHolder.getContextHolderStrategy()
        val securityContext = securityContextHolderStrategy.createEmptyContext()
        securityContext.authentication = authentication
        securityContextHolderStrategy.context = securityContext
    }

    private fun getBearerToken(request: HttpServletRequest?): String? {
        val token = request?.getHeader(AUTHORIZATION) ?: return null
        if (!token.startsWith(BEARER_TOKEN_PREFIX)) return null
        return token.removePrefix(BEARER_TOKEN_PREFIX)
    }

    companion object {
        const val BEARER_TOKEN_PREFIX = "Bearer "
    }

}


// HttpServletRequest faqat bir marta o'qilgani uchun Cachedga olingan
// https://stackoverflow.com/questions/65790323/how-to-get-request-body-params-in-spring-filter
class CachedBodyHttpServletRequest(request: HttpServletRequest) : HttpServletRequestWrapper(request) {

    private val cachedBody: ByteArray = request.inputStream.readBytes()

    override fun getInputStream(): ServletInputStream {
        return CachedBodyServletInputStream(cachedBody)
    }

    override fun getReader(): BufferedReader {
        return BufferedReader(InputStreamReader(ByteArrayInputStream(cachedBody), characterEncoding ?: "UTF-8"))
    }

    private class CachedBodyServletInputStream(private val cachedBody: ByteArray) : ServletInputStream() {
        private val inputStream = ByteArrayInputStream(cachedBody)

        override fun isFinished(): Boolean = inputStream.available() <= 0

        override fun isReady(): Boolean = true

        override fun setReadListener(readListener: ReadListener?) {
            // Not implemented
        }

        override fun read(): Int = inputStream.read()
    }
}

class CachedBodyHttpServletResponse(response: HttpServletResponse) : HttpServletResponseWrapper(response) {

    val customOutputStream = ByteArrayOutputStream()
    private val writer = PrintWriter(OutputStreamWriter(customOutputStream, Charsets.UTF_8))
    private var outputStreamUsed = false
    private var writerUsed = false

    override fun getOutputStream(): ServletOutputStream {
        if (writerUsed) {
            throw IllegalStateException("getWriter() has already been called for this response")
        }
        outputStreamUsed = true
        return object : ServletOutputStream() {
            override fun write(b: Int) {
                customOutputStream.write(b)
            }

            override fun isReady() = true

            override fun setWriteListener(listener: WriteListener?) {}
        }
    }

    override fun getWriter(): PrintWriter {
        if (outputStreamUsed) {
            throw IllegalStateException("getOutputStream() has already been called for this response")
        }
        writerUsed = true
        return writer
    }

    fun copyBodyToResponse() {
        if (writerUsed) {
            customOutputStream.flush()
        }
        val responseOutputStream = response.outputStream
        responseOutputStream.write(customOutputStream.toByteArray())
        responseOutputStream.flush()
    }
}
