package uz.davrbank.compliance

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.support.ResourceBundleMessageSource
import org.springframework.kafka.support.converter.StringJsonMessageConverter
import org.springframework.security.core.context.SecurityContextHolder

@Configuration
class WebConfigs {

    @Bean
    fun messageMessageSource() = ResourceBundleMessageSource().apply {
        setDefaultEncoding(Charsets.UTF_8.name())
        setBasename("error")
    }

    @Bean
    fun stringJsonMessageConverter(): StringJsonMessageConverter {
        return StringJsonMessageConverter()
    }
}


