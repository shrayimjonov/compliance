# compliance-service

Main compliance service. Checks transfers, checks core banking documents on a schedule, keeps watch lists, stores attachments in MinIO, logs API calls and builds Excel reports.

Stack: Kotlin, Java 21, Spring Boot 4.0.5, PostgreSQL, MongoDB, Elasticsearch, MinIO, WebFlux, Vault, Apache POI, Gradle.

Run:

```bash
./gradlew bootRun
```

Settings come from environment variables.
