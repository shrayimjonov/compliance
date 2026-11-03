package uz.davrbank.compliance.kafka

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.kafka.listener.DefaultErrorHandler
import org.springframework.kafka.support.converter.StringJsonMessageConverter
import org.springframework.util.backoff.FixedBackOff

@Configuration
class KafkaConfig(
    private val topicRepository: TopicRepository
) {

    @Bean
    @Primary
    fun jsonConvertor() = StringJsonMessageConverter()


    @Bean
    fun errorHandler() = DefaultErrorHandler({ t, u ->
        topicRepository.save(
            Topic(
                t.topic(),
                t.value(),
                t.value().javaClass.name,
                u.stackTraceToString())
        )
    }, FixedBackOff(500, 3))

}