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
Configuration comes from environment variables. See `src/main/resources/application.yml` and `../.env.example`.
