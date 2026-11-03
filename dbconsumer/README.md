# dbconsumer

Reads changes from Kafka and keeps the Elasticsearch indexes up to date.

Stack: Kotlin, Java 21, Spring Boot 3.4.4, PostgreSQL, MongoDB, Elasticsearch, Kafka, Vault, Gradle.

Run:

```bash
./gradlew bootRun
```

Settings come from environment variables.
