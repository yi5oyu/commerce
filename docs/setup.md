# 개발 환경 설정

로컬 개발 환경 구성과 실행 방법을 정리한 문서

---

## 기술 스택

| 구분 | 기술 | 버전 |
|---|---|---|
| 언어 | Java | 25 (LTS, Eclipse Temurin) |
| 백엔드 | Spring Boot | 4.1.2 |
| 빌드 | Gradle (Groovy DSL) | 9.x (Wrapper) |
| ORM | Hibernate | Spring Boot 관리 버전 (7.x) |
| DB | PostgreSQL | 18 |
| 테스트 DB | Testcontainers | Spring Boot 관리 버전 |
| 프론트엔드 | Next.js (App Router) + TypeScript | 16.x |
| 런타임 | Node.js | 24 (LTS) |
| 스타일 | Tailwind CSS | create-next-app 기본값 |
| 컨테이너 | Docker Desktop (WSL2 백엔드) + Docker Compose v2 | |
| 에디터 | VS Code | |

---

## 구조

```
commerce/
├─ .gitattributes        줄바꿈 규칙 (LF 통일)
├─ .gitignore            공통 제외 항목 (IDE, OS, .env)
├─ .editorconfig         에디터 작성 규칙
├─ backend/              Spring Boot (Gradle 단일 모듈)
├─ frontend/             Next.js
├─ infra/
│   └─ db/               로컬 PostgreSQL (db 서버 역할)
└─ docs/                 문서
```

---

## 사전 준비

### JDK 25

- [Eclipse Temurin 25 (JDK, x64, .msi) 설치](adoptium.net)
- 설치 시 `Add to PATH`, `Set JAVA_HOME variable` 선택


확인:
```powershell
java -version
javac -version
$env:JAVA_HOME
```

### Node.js 24

- [nodejs.org에서 LTS(24.x) 설치](nodejs.org)
- `frontend/.nvmrc`에 메이저 버전만 기록: `24`

확인:
```powershell
node -v
```

### VS Code 확장

| 확장 | 용도 |
|---|---|
| Extension Pack for Java (Microsoft) | Java 언어 지원, 디버깅, Gradle 연동 |
| Spring Boot Extension Pack | Spring 설정 자동완성, Spring Boot Dashboard |
| EditorConfig for VS Code | `.editorconfig` 적용 |
| PostgreSQL (Microsoft) | DB 조회 GUI |

- `Extension Pack for Java Auto Config`는 설치하지 않음 (사용자 설정을 일괄 변경하므로)
- JDK 연결 확인: `Ctrl + Shift + P` → `Java: Configure Java Runtime`
- `build.gradle` 수정 후 반영: 파일 우클릭 → `Reload Projects` (또는 `Java: Reload Projects`)

---

## 공통 설정

### `.gitattributes`

Windows(CRLF)와 Linux 서버·컨테이너(LF)의 줄바꿈 차이로 인한 실행 오류를 막기 위해 LF로 통일

```
# 텍스트 파일은 전부 LF로 저장
* text=auto eol=lf

# Windows 전용 스크립트만 CRLF 유지
*.bat text eol=crlf
*.cmd text eol=crlf
*.ps1 text eol=crlf

# 이미지, jar는 변환하지 않음
*.jar binary
*.png binary
*.jpg binary
*.ico binary
```

> backend/ .gitattributes

### `.editorconfig`

```ini
root = true

[*]
charset = utf-8
end_of_line = lf
insert_final_newline = true
trim_trailing_whitespace = true
indent_style = space
indent_size = 2

[*.{java,kts,gradle}]
indent_size = 4

[*.{bat,cmd,ps1}]
end_of_line = crlf

[*.md]
trim_trailing_whitespace = false
```

### `.gitignore` 

루트에는 공통 항목 처리   

`backend/`, `frontend/` 등 각 폴더 예외 처리

```
# IDE
.idea/
.vscode/*
!.vscode/launch.json
*.iml

# OS
.DS_Store
Thumbs.db

# 환경변수
.env
.env.*
!.env.example
```

---

## 로컬 DB (`infra/db`)

앱 서버와 DB 서버를 분리하는 구조를 로컬에서도 같게 맞추기 위해 DB를 독립된 compose 스택으로 관리

### 설정

- `127.0.0.1:5432`: 내 PC에서만 접속 가능
- 볼륨 경로 `/var/lib/postgresql`: PostgreSQL 18 이미지부터 데이터 위치 바뀜. 예전 경로(`/var/lib/postgresql/data`)는 사용하지 않음
- `healthcheck`: 접속 가능 상태 확인. 추후 app 스택의 시작 순서 제어에 사용

### 명령어 (저장소 루트에서 실행)

| 동작 | 명령 | 데이터 |
|---|---|---|
| 켜기 | `docker compose -f infra/db/compose.yaml up -d` | |
| 상태 확인 | `docker compose -f infra/db/compose.yaml ps` | |
| 잠시 끄기 | `docker compose -f infra/db/compose.yaml stop` | 유지 |
| 컨테이너 삭제 | `docker compose -f infra/db/compose.yaml down` | 유지 |
| 데이터까지 삭제 | `docker compose -f infra/db/compose.yaml down -v` | 삭제 |
| psql 접속 | `docker exec -it commerce-postgres psql -U commerce -d commerce` | |

### GUI 접속 (VS Code PostgreSQL 확장)

| 항목 | 값 |
|---|---|
| Server name | `127.0.0.1` |
| Port | `5432` |
| User / Password | `commerce` / `local` |
| Database | `commerce` |

---

## 백엔드 (`backend/`)

### Spring Initializr 설정

| 항목 | 값 |
|---|---|
| Project | Gradle - Groovy |
| Language | Java |
| Spring Boot | 4.1.2 (정식 버전) |
| Group | `io.github.yi5oyu` |
| Artifact / Name | `commerce` |
| Package name | `io.github.yi5oyu.commerce` |
| Packaging | Jar |
| Configuration | YAML |
| Java | 25 |

#### 의존성  

| 의존성 | 용도 |
|---|---|
| Spring Web | REST API |
| Spring Security | 인증·인가 |
| OAuth2 Resource Server | JWT 검증 (커스텀 JWT 필터 대체) |
| Spring Data JPA | ORM |
| PostgreSQL Driver | DB 드라이버 |
| Flyway Migration | 스키마 버전 관리 |
| Validation | 입력 검증 |
| Spring Boot Actuator | 헬스체크, 메트릭 |
| Testcontainers | 테스트용 실제 PostgreSQL |

> Lombok

### 실행 설정

**터미널** (`backend/`에서)

```powershell
.\gradlew bootRun --args="--spring.profiles.active=local"
```
종료: `Ctrl + C` → `Y`

**VS Code** (`.vscode/launch.json`, 저장소 루트)

```json
{
  "version": "0.2.0",
  "configurations": [
    {
      "type": "java",
      "name": "Commerce (local)",
      "request": "launch",
      "mainClass": "io.github.yi5oyu.commerce.CommerceApplication",
      "cwd": "${workspaceFolder}/backend",
      "env": { "SPRING_PROFILES_ACTIVE": "local" }
    }
  ]
}
```
- 실행 `F5` / 정지 `Shift + F5` / 재시작 `Ctrl + Shift + F5`
- Spring Boot Dashboard에서도 실행·정지 가능 (실행 상태 표시)

### DB 스키마 (Flyway)

스키마는 Flyway 마이그레이션으로만 변경하고, Hibernate는 검증만 함

```yaml
# application.yaml
spring:
  jpa:
    hibernate:
      ddl-auto: validate   # 엔티티와 테이블이 다르면 실행 시 에러
```

- 위치: `src/main/resources/db/migration/`
- 파일명: `V{번호}__{설명}.sql` (예: `V1__create_member.sql`, 밑줄 2개)
- 한 번 적용된 파일은 수정하지 않고 새 버전 파일 추가
- 첫 서버 배포 전까지는 기존 파일 수정 허용. 수정 후 로컬 DB 초기화: `docker compose -f infra/db/compose.yaml down -v`
- 테스트(Testcontainers)에서도 동일하게 마이그레이션 적용

### 테스트 (Testcontainers)

`src/test/java/io/github/yi5oyu/commerce/TestcontainersConfiguration.java`의 이미지를 운영과 같은 버전으로 고정

```java
return new PostgreSQLContainer(DockerImageName.parse("postgres:18"));
```

실행 (`backend/`에서, Docker 실행 중이어야 함)

```powershell
.\gradlew test
.\gradlew build
```

---

## 프론트엔드 (`frontend/`)

### 생성

```powershell
npx create-next-app@latest frontend
```

`Yes, use recommended defaults` 선택  
TypeScript, ESLint, Tailwind CSS, App Router, Cache Components, `src/` 디렉터리 없음, React Compiler 없음, AGENTS.md

### `next.config.ts`

생성기가 넣은 설정은 유지하고 `output`, `rewrites` 추가

```ts
import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  experimental: {
    agentFeedback: true,
  },
  cacheComponents: true,
  partialPrefetching: true,
  turbopack: {
    rules: {
      "*.css": {
        loaders: ["@tailwindcss/turbopack"],
        as: "*.css",
      },
    },
  },
  output: "standalone",
  async rewrites() {
    if (process.env.NODE_ENV !== "development") return [];
    return [
      {
        source: "/api/:path*",
        destination: "http://localhost:8080/api/:path*",
      },
    ];
  },
};

export default nextConfig;
```

> `output: "standalone"`: 작은 Docker 이미지 빌드용
> 개발용 `rewrites`: 브라우저의 `/api` 요청을 Spring으로 전달. 운영에서 Nginx가 할 역할을 로컬에서 재현, 같은 출처가 되어 CORS 설정 필요 없고 httpOnly 쿠키도 운영과 같은 방식으로 동작

### 환경변수

```
API_INTERNAL_URL=http://localhost:8080
```

- `API_INTERNAL_URL`: 서버 컴포넌트가 Spring을 직접 호출할 때 사용
- 브라우저 요청은 `/api` 상대 경로를 쓰므로 별도 변수 불필요
- `NEXT_PUBLIC_` 접두사는 브라우저에 노출해도 되는 값에만 사용
- `frontend/.gitignore`의 `.env*` 규칙이 예시 파일까지 제외하므로 아래 줄 추가
  ```
  !.env.example
  ```

### 실행

```powershell
npm install
npm run dev      # http://localhost:3000
npm run build
```

Node 버전 변경 후 `node_modules` 지우고 다시 설치

```powershell
npm install
```

---

## 로컬 개발 실행 순서

1. Docker Desktop 실행
2. DB 컨테이너 실행 (루트): `docker compose -f infra/db/compose.yaml up -d`
3. Spring 실행 (local 프로필): VS Code `F5` 또는 `gradlew bootRun`
4. Next.js 실행 (`frontend/`): `npm run dev`

DB는 켜두고, Spring과 Next.js만 필요할 때 재시작

> Docker 명령은 저장소 루트, Gradle 명령은 `backend/`, npm 명령은 `frontend/` 실행
