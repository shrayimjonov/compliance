# Compliance

KYC/AML check system for a bank. Before a transfer goes through, it checks the customer and the receiver
against sanction lists and internal watch lists. The search runs on Elasticsearch and the goal is to answer
in about 100 ms while handling thousands of requests.

This is a cleaned copy of the real project. Passwords, server addresses and personal data were removed.

## Modules

- compliance-service - main service. Checks transfers, checks documents from core banking on a schedule,
  keeps watch list dictionaries, stores attachments in MinIO, logs API calls, builds Excel reports
- dbconsumer - reads changes from Kafka and keeps the Elasticsearch indexes up to date
- rest-service - search API used by other bank systems

## Search

- search by several fields at once
- name cleanup and Cyrillic to Latin transliteration
- fuzzy and wildcard search
- results tuned for accuracy and speed

API calls are logged with an AOP aspect (`@LogApiCall`).

## Stack

Kotlin, Spring Boot 3/4 (Java 17/21), Elasticsearch, Kafka, PostgreSQL, MongoDB, MinIO, Vault, Eureka,
WebFlux, Apache POI, Docker, Gradle.

## How to run

```bash
cd compliance-service
./gradlew bootRun
```

The real settings (databases, Elasticsearch, Kafka, Vault, MinIO) were in profile files that were removed,
so you need to add your own profile before running.

## What was removed

Passwords, keys, config profiles, CI files, internal hosts, personal data and git history.
