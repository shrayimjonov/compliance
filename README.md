# Compliance — Real-time KYC / AML Screening Platform

High-load compliance system at Davrbank that screens customers and transfers **in real time before a transaction
is executed**: it matches names, documents and organizations against sanction and internal watch lists, using
Elasticsearch full-text and fuzzy search.
Target latency is **≤100 ms per check** while handling thousands of requests.

> **Context.** Built at Davrbank, where I work as a Backend Engineer (Oct 2025 – present). I was the main
> developer of `compliance-service` (≈158 of 197 commits) and built the customer-search functionality on
> Elasticsearch. Published as a portfolio sample with confidential data removed.

## Modules

| Module | Purpose |
|---|---|
| [`compliance-service`](compliance-service) | Core service. Pre-transaction checks for transfers, core-banking (ABS) document checker, watch-list dictionaries, attachments (MinIO), API audit logging, Excel reports |
| [`dbconsumer`](dbconsumer) | Kafka consumer that keeps the Elasticsearch indices in sync with the source databases (CDC-style ingestion) |
| [`rest-service`](rest-service) | Search/screening REST API with bearer-token security, used by other banking channels |

## How a check works

```
transfer request ─▶ compliance-service ─▶ normalize & transliterate names (Cyrillic ⇄ Latin)
                                       ─▶ Elasticsearch query (exact + fuzzy + wildcard, scored)
                                       ─▶ screening result + audit log
source DBs ─▶ Kafka ─▶ dbconsumer ─▶ Elasticsearch indices (kept up to date)
```

## What I built
- Customer search on **Elasticsearch**: multi-field queries, name normalization and Cyrillic/Latin
  transliteration, fuzzy and wildcard matching, and relevance tuning for accuracy and speed.
- Pre-transaction screening endpoints and the ABS (core banking) document checker with a scheduled poller.
- API-call audit logging via an AOP aspect (`@LogApiCall`).

## Tech stack
**Kotlin · Spring Boot 3.3–4.0 (Java 17/21) · Elasticsearch · Kafka · PostgreSQL · MongoDB · MinIO ·
HashiCorp Vault · Spring Cloud (Eureka) · WebFlux · Apache POI · Docker · Gradle (Kotlin DSL)**

## Running

```bash
cd compliance-service && ./gradlew bootRun
```
Environment-specific configuration (datasources, Elasticsearch, Kafka, Vault, MinIO endpoints and credentials) lived in
`application-<profile>.yml` files that were removed from this copy, so you need to supply your own profile before running.

## Security & sanitization
Credentials, keys, environment profiles, CI pipelines, internal hosts and personal data were removed;
git history is not included.
