package uz.davrbank.compliance.util

import mu.KotlinLogging
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.core.env.ConfigurableEnvironment
import org.springframework.stereotype.Component
import java.net.InetAddress
import java.net.UnknownHostException
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

@Component
class StartupLogger(private val environment: ConfigurableEnvironment) {
    private val logger = KotlinLogging.logger {}

    @EventListener(ApplicationReadyEvent::class)
    fun logStartup() {
        logger.info { "Compliance  Service Start : ${formattedDate()}" }
        logger.info(
            """


                        ____________________COMPLIANCE______________________________

                        Compliance  Service started SUCCESSFULLY
                        author: Shukhratjon Rayimjonov 
                        application: ..........: ${applicationName()}
                        active profiles: ......: ${activeProfiles()}
                        address: ..............: ${serviceAddress()}
                        swagger-url: ..............: ${serviceAddress()}/swagger-ui/index.html
                        _____________________________________________________
            """.trimIndent()
        )
    }

    private fun formattedDate(): String {
        val date = Date()
        val dateFormat: DateFormat = SimpleDateFormat("dd-MM-yyyy :> hh:mm:ss a", Locale.US)
        dateFormat.timeZone = TimeZone.getTimeZone("Asia/Tashkent")
        return dateFormat.format(date)
    }

    private fun activeProfiles(): String {
        val profiles = environment.activeProfiles
        return if (profiles.isNotEmpty()) profiles.joinToString() else "local"
    }

    private fun applicationName(): String {
        return environment.getProperty("spring.application.name") ?: "compliance-service"
    }

    private fun serviceAddress(): String {
        val host = getHostAddress()
        val port = environment.getProperty("local.server.port")
            ?: environment.getProperty("server.port")
            ?: "8080"
        return "http://$host:$port"
    }

    private fun getHostAddress(): String {
        return try {
            if (activeProfiles() == "local") {
                return "localhost"
            }
            InetAddress.getLocalHost().hostAddress
        } catch (ex: UnknownHostException) {
            logger.error(ex) { "Can't determine host address" }
            "localhost"
        }
    }
}
