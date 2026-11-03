package uz.davrbank.compliance.dbconsumer.kafka

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.listener.DefaultErrorHandler
import org.springframework.kafka.support.converter.JsonMessageConverter
import org.springframework.kafka.support.converter.RecordMessageConverter
import org.springframework.util.backoff.FixedBackOff
import uz.davrbank.compliance.dbconsumer.domain.mongodb.Topic
import uz.davrbank.compliance.dbconsumer.repository.mongodb.TopicRepository

@Configuration
class ErrorHandlerConfigs(
    private val topicRepository: TopicRepository
) {

    @Bean
    fun recordMessageConverter(): RecordMessageConverter {
        return JsonMessageConverter()
    }

    @Bean
    fun errorHandler(): DefaultErrorHandler {
        val errorHandler = DefaultErrorHandler({ record, exception ->
            val payload = record.value()

            val topicError = Topic(
                topicName = record.topic(),
                topic = payload?.toString() ?: "NULL_PAYLOAD",
                topicType = payload?.javaClass?.name ?: "Unknown",
                exceptionMessage = exception.cause?.message ?: exception.message ?: "Unknown Error"
            )

            topicRepository.save(topicError)
        }, FixedBackOff(500L, 3))

        errorHandler.isAckAfterHandle = true
        return errorHandler
    }
}