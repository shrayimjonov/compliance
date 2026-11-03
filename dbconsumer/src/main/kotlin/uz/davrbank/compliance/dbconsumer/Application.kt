package uz.davrbank.compliance.dbconsumer

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableJpaRepositories
@EnableKafka
@EnableScheduling
class Application

fun main(args: Array<String>) {
    runApplication<Application>(*args)
}
