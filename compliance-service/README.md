# compliance-service

Core screening service of the [Compliance platform](../README.md).

- Pre-transaction compliance checks for transfers
- Core-banking (ABS) document checker with a scheduled poller
- Watch-list dictionaries and entity translation (Cyrillic ⇄ Latin)
- Elasticsearch search API, attachments in MinIO, API audit logging (`@LogApiCall` aspect), Excel reports

**Stack:** Kotlin, Spring Boot 4, Java 21, Elasticsearch, PostgreSQL, MongoDB, MinIO, Vault, Eureka, WebFlux.

```bash
./gradlew bootRun
```
Environment-specific configuration (`application-<profile>.yml`) was removed from this copy; provide your own profile
(datasource, Elasticsearch, Kafka, MinIO, Vault) before running.
