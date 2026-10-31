<!-- Procedure lives in the root AGENTS.md. This file holds only this folder's facts. -->

# backend

## Commands

Run from `backend/`. Tests need Docker running (Testcontainers).

- Build: `./gradlew build -x test`
- Test: `./gradlew test`
- Single test: `./gradlew test --tests "*CommerceApplicationTests*"` (unverified)
- Run locally: `./gradlew bootRun --args="--spring.profiles.active=local"` (unverified) — needs the local DB up first (from repo root: `docker compose -f infra/db/compose.yaml up -d`)

## Structure

- Owns the commerce REST API, auth (JWT via OAuth2 Resource Server), and the DB schema
- Stack: Java 25, Spring Boot 4.1 (webmvc · data-jpa · security · validation · actuator), PostgreSQL 18, Lombok
- Base package: `src/main/java/io/github/yi5oyu/commerce/`
- Schema changes only through Flyway: `src/main/resources/db/migration/V{n}__{description}.sql`; Hibernate runs with `ddl-auto: validate`
- Profiles: `application.yaml` (common), `application-local.yml` (local DB at `localhost:5432`)
- Tests boot a `postgres:18` container via `TestcontainersConfiguration` — import it in `@SpringBootTest` tests
