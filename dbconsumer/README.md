# dbconsumer

Kafka consumer of the [Compliance platform](../README.md). It reads change events from the source databases
and keeps the Elasticsearch indices used for screening up to date.

**Stack:** Kotlin, Spring Boot 3.4, Java 21, Kafka, Elasticsearch, PostgreSQL, MongoDB, Vault.

```bash
./gradlew bootRun
```
