<!-- Procedure lives in the root AGENTS.md. This file holds only this folder's facts. -->

# backend

## Commands

Run from `backend/`. Tests need Docker running (Testcontainers).

- Build: `./gradlew build -x test`
- Test: `./gradlew test`
- Single test: `./gradlew test --tests "*CommerceApplicationTests*"`
- Run locally: `./gradlew bootRun --args="--spring.profiles.active=local"` — needs the local DB up first (from repo root: `docker compose -f infra/db/compose.yaml up -d`)
- Regenerate OpenAPI spec: `./gradlew openApiSpec` — rewrites `backend/openapi.json`; run after changing any API, then `npm run gen:api` in `frontend/`. `./gradlew test` fails while the spec is stale

## Structure

- Owns the commerce REST API, auth (JWT via OAuth2 Resource Server), and the DB schema
- Stack: Java 25, Spring Boot 4.1 (webmvc · data-jpa · security · validation · actuator), PostgreSQL 18, Lombok
- Base package: `src/main/java/io/github/yi5oyu/commerce/`
- Packages: by domain first (`member/`, `product/` …), layers inside each (`controller/` `service/` `repository/` `entity/` `dto/` `exception/`); cross-domain shared code only in `common/`
- Cross-domain references by ID only — no JPA associations across domain packages; call the other domain's service instead
- Errors: throw `BusinessException(SomeErrorCode.X)` — common codes in `CommonErrorCode`, domain codes in a per-domain enum implementing `ErrorCode`; `common/error/GlobalExceptionHandler` turns every error into `ProblemDetail` + `code` (+ `errors` on validation failure)
- Security: `common/security/SecurityConfig` — stateless, every path requires authentication except `PUBLIC_PATHS`; add new public endpoints there. 401/403 are written as `ProblemDetail` by the entry point / access-denied handler in the same package
- Schema changes only through Flyway: `src/main/resources/db/migration/V{n}__{description}.sql`; Hibernate runs with `ddl-auto: validate`
- Profiles: `application.yaml` (common), `application-local.yml` (local DB at `localhost:5432`)
- Tests boot a `postgres:18` container via `TestcontainersConfiguration` — import it in `@SpringBootTest` tests
