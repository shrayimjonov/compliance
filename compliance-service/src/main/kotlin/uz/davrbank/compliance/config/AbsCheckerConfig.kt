package uz.davrbank.compliance.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.EnableAspectJAutoProxy
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import java.util.concurrent.Executor

@Configuration
@EnableAsync
@EnableAspectJAutoProxy
@EnableScheduling
@EnableConfigurationProperties(AbsCheckerProperties::class)
class AbsCheckerConfig {

    @Bean("absCheckerExecutor")
    fun absCheckerExecutor(): Executor = ThreadPoolTaskExecutor().apply {
        corePoolSize = 2
        maxPoolSize = 5
        queueCapacity = 200
        setThreadNamePrefix("abs-checker-")
        setWaitForTasksToCompleteOnShutdown(true)
        setAwaitTerminationSeconds(30)
        initialize()
    }

    @Bean("logExecutor")
    fun logExecutor(): Executor = ThreadPoolTaskExecutor().apply {
        corePoolSize = 1
        maxPoolSize = 2
        queueCapacity = 500
        setThreadNamePrefix("api-log-")
        setWaitForTasksToCompleteOnShutdown(true)
        setAwaitTerminationSeconds(30)
        initialize()
    }
}

@ConfigurationProperties(prefix = "application.abs-checker")
data class AbsCheckerProperties(
    val batchSize: Int = 50,
    val pollDelayMs: Long = 300_000,
    val maxStoredHits: Int = 10_000,
    val logBodyLimit: Int = 100_000,
)
