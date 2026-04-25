# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Test Commands

```bash
./gradlew build                        # Full build all modules
./gradlew clean build                  # Clean then build
./gradlew :Mavis-Api:test              # Test specific module
./gradlew :Mavis-Domain:test           # Domain integration tests (uses Testcontainers + MySQL)
./gradlew :Mavis-Api:bootRun           # Run user API (port 8080)
./gradlew :Mavis-Admin:bootRun         # Run admin API (port 8081)
```

## Module Architecture

Dependency flows bottom-up:

```
Mavis-Common  →  Mavis-Domain  →  Mavis-Infrastructure  →  Mavis-Api / Mavis-Admin
```

| Module | Role |
|--------|------|
| **Mavis-Common** | Shared DTOs, JWT util, `MavisException`/`ErrorCode`, config properties, utilities |
| **Mavis-Domain** | JPA entities, Spring Data JPA + QueryDSL repositories, `PaymentIdempotencyManager` |
| **Mavis-Infrastructure** | Feign clients for OAuth (Kakao/Naver), TossPayments, S3, Discord; Spring Mail templates |
| **Mavis-Api** | User REST API on port 8080 — auth, cart, orders, reviews, refunds, etc. |
| **Mavis-Admin** | Admin REST API on port 8081 — product CRUD, order/delivery/refund management, Excel export |

## Key Patterns

**Exception handling:** All errors throw `MavisException(ErrorCode)`. `ErrorCode` enums are defined per domain package.

**Repository pattern:** Complex queries use `*RepositoryCustom` interface + `*RepositoryImpl` with `JPAQueryFactory`. Q-classes are eagerly loaded in `MavisApiServerApplication` via `Class.forName`.

**Service decomposition:**
- `*Reader` — read-only queries
- `*Appender` / `*Modifier` — write operations
- `*Facade` — orchestrates multiple services (e.g., `OrderFacade`)

**External API errors:** Feign error decoders (e.g., `TossPaymentsErrorDecoder`) convert external HTTP errors to `MavisException`.

**Sensitive configuration:** DB credentials, OAuth secrets, TossPayments keys live in `Mavis-Submodule` (separate Git submodule, not committed here).

## Tech Stack

- **Java 21**, Spring Boot 3.5.4, Gradle 8.14.3
- **ORM:** JPA/Hibernate + QueryDSL 5.0.0
- **Auth:** JJWT 0.12.6, Spring Security (separate user vs admin contexts)
- **HTTP clients:** Spring Cloud OpenFeign (Spring Cloud 2025.0.0)
- **Storage:** AWS S3 SDK 2.29.50
- **Testing:** JUnit + Testcontainers 1.20.4 (MySQL)
- **DB:** MySQL (prod), H2 (local)
