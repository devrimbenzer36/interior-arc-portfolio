# Portfolio API

Spring Boot backend for a professional interior design portfolio platform.
The system serves a public-facing portfolio site and an admin panel for managing projects, media, and contact messages.

---

## What This System Does

- Serves published interior design **projects** to visitors (filtered, paginated, by slug)
- Provides an **admin panel API** for creating/editing projects, uploading images, and managing contact messages
- Handles **file uploads** with a storage abstraction that supports local disk now and S3 later — zero code change required to switch
- Tracks **contact form messages** with status management (NEW → READ → REPLIED)

---

## Architecture Overview

### Layered Structure

```
HTTP Request
    ↓
Controller      — HTTP binding only. No business logic.
    ↓
Service         — Business rules, orchestration, transaction boundary.
    ↓
Repository      — Data access. Spring Data JPA + custom JPQL queries.
    ↓
Entity          — State + domain methods. Business invariants live here.
```

**Rule:** Controllers never touch repositories. Services never build HTTP responses. Entities never call repositories.

### Domain-Based Package Structure

```
com.portfolio
├── config/                  # SecurityConfig, OpenApiConfig
├── security/                # JWT auth infrastructure
│   ├── filter/              # JwtAuthFilter (OncePerRequestFilter)
│   ├── handler/             # AuthEntryPoint (401), AccessDeniedHandlerImpl (403)
│   ├── service/             # UserDetailsServiceImpl
│   └── util/                # JwtUtil — generate / parse / validate
├── common/
│   ├── exception/           # GlobalExceptionHandler, BusinessException, ResourceNotFoundException
│   ├── response/            # ApiResponse<T> — unified response wrapper
│   └── util/                # SlugUtil
└── domain/
    ├── admin/               # Auth domain — AdminUser entity, login endpoint
    │   ├── controller/      # AuthController (POST /api/v1/auth/login)
    │   ├── service/         # AuthService, DataInitializer
    │   ├── repository/
    │   ├── entity/
    │   └── dto/request+response/
    ├── project/             # Core domain — projects, images, tags, materials
    │   ├── controller/
    │   ├── service/
    │   ├── repository/
    │   ├── entity/
    │   ├── dto/request/
    │   ├── dto/response/
    │   └── enums/
    ├── media/               # File upload, storage abstraction, file serving
    │   ├── controller/      # MediaController (upload), FileServeController (GET /files)
    │   ├── service/         # MediaService, StorageService (interface), LocalStorageService
    │   ├── repository/
    │   ├── entity/
    │   └── enums/
    └── contact/             # Contact form messages
        ├── controller/
        ├── service/
        ├── repository/
        ├── entity/
        └── enums/
```

### Storage Architecture

```
MediaController.upload()
    → StorageService.upload()          ← interface
        → LocalStorageService          ← active in Phase 1 (app.storage.type=local)
        → S3StorageService             ← Phase 2   (app.storage.type=s3)
    → MediaFile saved to DB            ← only metadata, never binary content
    → returns MediaFileResponse
```

---

## Modules

### `domain/project`

The core domain. `Project` is the aggregate root.

| Class | Responsibility |
|---|---|
| `Project` | Entity with domain methods (`publish`, `unpublish`, `addImage`, `removeImageById`, `applyPatch`) |
| `ProjectImage` | Join between Project and MediaFile with ordering and type |
| `ProjectTag` / `ProjectMaterial` | Value-object-like children, cascade ALL |
| `ProjectService` | Orchestrates create/update/publish, resolves slugs, syncs tags and materials |
| `ProjectRepository` | Custom `@EntityGraph` queries to prevent N+1 on cover image and collections |
| `ProjectImageRepository` | `JOIN FETCH` query to load images + media in one query |

**Cascade rules:**
- `images`, `tags`, `materials` → `CASCADE ALL + orphanRemoval` — deleted with project
- `coverImage` → FK only, no cascade — media file is independent

**PATCH semantics on update:** `null` field = keep existing value. Empty list `[]` = clear. Non-null value = update.

### `domain/media`

Handles file lifecycle. The binary file lives on disk (or S3). The DB stores only metadata.

| Class | Responsibility |
|---|---|
| `MediaService` | Validates, uploads via StorageService, saves MediaFile record |
| `LocalStorageService` | Writes to `uploads/` directory, serves via `FileServeController` |
| `MediaFile` | Metadata: originalName, storedName (UUID), url, mimeType, size, width, height |
| `FileServeController` | `GET /files/{filename}` — serves local files with path traversal protection |

**Security in file upload:**
- MIME type checked against allowlist (jpeg, png, webp, gif)
- Stored filename is UUID — original filename never touches the filesystem
- Path traversal: `resolveSecurePath()` normalizes and verifies path stays within upload dir

### `domain/contact`

Stateless contact form. Visitors submit, admin reads and updates status.

| Class | Responsibility |
|---|---|
| `ContactService` | `sendMessage()` saves with IP, admin methods list/filter/update/delete |
| `ContactMessage` | Entity with status machine: `NEW → READ → REPLIED` |
| `EmailNotificationService` | Async HTML email to configured recipients on new submission. Spam detection flags suspicious messages in subject line. |

### `audit`

Immutable audit trail for all admin and public actions.

| Class | Responsibility |
|---|---|
| `AuditLogService` | Fluent builder: `record(action).entity(...).ip(...).meta(...).save()` |
| `AsyncAuditPersister` | Separate `@Component` to ensure Spring proxy — `@Async("auditExecutor")` fires correctly |
| `AuditLog` | Immutable entity. No setters. Factory method `AuditLog.of(...)`. |
| `AuditAction` | Enum: LOGIN_SUCCESS/FAILURE/BLOCKED, PROJECT_*, CONTACT_*, MEDIA_* |

### `common`

| Class | Responsibility |
|---|---|
| `ApiResponse<T>` | Unified JSON wrapper: `{ success, data, message, errors }` |
| `GlobalExceptionHandler` | Catches `BusinessException` (4xx), `ResourceNotFoundException` (404), `DataIntegrityViolationException` (409), `MethodArgumentNotValidException` (400) |
| `BusinessException` | Carries HTTP status — service layer throws this for rule violations |
| `SlugUtil` | Turkish-aware slug generator: `"Boğaziçi Dairesi"` → `"bogazici-dairesi"` |

---

## API Overview

All responses are wrapped: `{ "success": true, "data": {...} }`

### Auth Endpoints

| Method | Path | Description |
|---|---|---|
| POST | `/api/v1/auth/login` | Admin login — returns JWT token |

**Login request:**
```json
{ "email": "admin@portfolio.com", "password": "admin123" }
```

**Login response:**
```json
{
  "success": true,
  "data": {
    "token": "<jwt>",
    "tokenType": "Bearer",
    "expiresIn": 86400,
    "email": "admin@portfolio.com",
    "role": "ADMIN"
  }
}
```

### Public Endpoints (no auth)

| Method | Path | Description |
|---|---|---|
| GET | `/api/v1/projects` | Published projects, paginated (size=12, sorted by date) |
| GET | `/api/v1/projects/featured` | Featured published projects |
| GET | `/api/v1/projects/category/{category}` | By category, paginated |
| GET | `/api/v1/projects/{slug}` | Project detail by slug, increments view count |
| GET | `/files/{filename}` | Serve uploaded file (local storage only) |
| POST | `/api/v1/contact` | Submit contact form |

### Admin Endpoints (JWT protected — ADMIN role required)

Send token as: `Authorization: Bearer <token>`

| Method | Path | Description |
|---|---|---|
| GET | `/api/v1/admin/projects` | All projects (all statuses), paginated |
| GET | `/api/v1/admin/projects/{id}` | Project detail by ID |
| POST | `/api/v1/admin/projects` | Create project (with optional coverImageId, imageIds) |
| PATCH | `/api/v1/admin/projects/{id}` | Partial update — null fields ignored |
| DELETE | `/api/v1/admin/projects/{id}` | Delete project |
| PATCH | `/api/v1/admin/projects/{id}/publish` | Publish |
| PATCH | `/api/v1/admin/projects/{id}/unpublish` | Unpublish |
| PATCH | `/api/v1/admin/projects/{id}/featured?value=true` | Set featured |
| PATCH | `/api/v1/admin/projects/{id}/cover-image/{mediaFileId}` | Set cover image |
| POST | `/api/v1/admin/projects/{id}/images/{mediaFileId}` | Add image to gallery |
| DELETE | `/api/v1/admin/projects/{id}/images/{imageId}` | Remove image from gallery |
| PUT | `/api/v1/admin/projects/{id}/images/reorder` | Reorder gallery images |
| POST | `/api/v1/admin/media/upload` | Upload file, returns MediaFileResponse |
| GET | `/api/v1/admin/contact` | List all contact messages, paginated |
| GET | `/api/v1/admin/contact/status/{status}` | Filter by status |
| GET | `/api/v1/admin/contact/count/new` | Count of NEW messages |
| PATCH | `/api/v1/admin/contact/{id}/status` | Update message status |
| DELETE | `/api/v1/admin/contact/{id}` | Delete message |

---

## Database

### Tables

| Table | Description |
|---|---|
| `projects` | Core project data, status, featured flag, view count |
| `project_images` | Gallery images — FK to both projects and media_files |
| `project_tags` | Project labels, cascade deleted with project |
| `project_materials` | Materials used, cascade deleted with project |
| `media_files` | Upload metadata only — binary lives on disk/S3 |
| `contact_messages` | Contact form submissions with status tracking |
| `admin_users` | Admin accounts — JWT auth active |
| `audit_logs` | Immutable audit trail — no delete/update methods on repository |
| `comments` | Visitor comments — table ready, entity in Phase 3 |

### Key Relationships

```
projects
  ├── cover_image_id → media_files   (FK, ON DELETE SET NULL)
  ├── project_images → media_files   (FK, ON DELETE RESTRICT)
  │         └── project_id → projects (ON DELETE CASCADE)
  ├── project_tags   → project_id   (ON DELETE CASCADE)
  └── project_materials → project_id (ON DELETE CASCADE)
```

Migrations are managed by **Flyway**. Hibernate is set to `ddl-auto: validate` — it verifies schema matches entities on startup, never modifies it.

---

## Key Technical Decisions

### Why `StorageService` is an interface

To swap storage backends without touching business logic. `LocalStorageService` is active when `app.storage.type=local`. When set to `s3`, it's deactivated via `@ConditionalOnProperty` and `S3StorageService` takes over. `MediaService` and every caller is completely unaware of the switch.

### Why `MediaFile` is a separate entity

Storing binary in the DB is a performance and backup disaster at scale. `MediaFile` holds only metadata: where the file is, how to access it, what its dimensions are. The actual bytes live on the filesystem (or S3 later). Additionally, a single `MediaFile` can be referenced by multiple projects (cover image, gallery) without duplication.

### Why PATCH instead of PUT for project update

A portfolio admin rarely updates all 12 fields at once. PUT would require sending the entire object every time. With PATCH: send only what changed. `null` = leave existing value untouched. Empty `[]` = clear the list. This is enforced in `Project.applyPatch()` — the entity itself handles the conditional field update.

### Why `@EntityGraph` instead of `FetchType.EAGER`

`EAGER` loading fires joins on every query regardless of need. `@EntityGraph` is per-query: list endpoints load only `coverImage` (needed for card rendering). Detail endpoints load `coverImage + tags + materials`. Gallery images are always fetched in a separate optimized `JOIN FETCH` query. Result: no N+1, no over-fetching.

### Why domain methods on `Project` entity

`project.setStatus("PUBLISHED")` is a setter — it has no opinion. `project.publish()` is a domain method — tomorrow it can enforce "must have at least one image" without any service changes. Business rules belong to the entity they protect.

---

## Local Development

### Prerequisites

- Java 17 (JDK, not JRE)
- Maven 3.x
- Docker Desktop

### 1. Start PostgreSQL

```bash
docker-compose up -d
```

This starts `portfolio_db` on `localhost:5432`. Flyway runs migrations automatically on first boot.

### 2. Run the backend

```bash
# Windows — set JAVA_HOME explicitly if multiple JDKs are installed
set JAVA_HOME=C:\Program Files\Java\jdk-17
cd apps/api
mvn spring-boot:run
```

Or run `PortfolioApplication.java` directly from IntelliJ with the `dev` profile active.

**Environment variables — IntelliJ run config (dev):**
```
APP_JWT_SECRET=<min 32 chars — e.g. openssl rand -base64 48>
APP_ADMIN_EMAIL=admin@portfolio.com
APP_ADMIN_PASSWORD=<strong password>
MAIL_USERNAME=interiorarcstudio@gmail.com
MAIL_APP_PASSWORD=<16-digit Gmail App Password>
APP_STORAGE_BASE_URL=http://localhost:8080/files
```

Gmail App Password: Google Account → Security → 2-Step Verification → App Passwords

**VM option:**
```
-Dspring.profiles.active=dev
```

**Production env vars (Railway):**
```
DATABASE_URL=<Railway postgres URL>
DATABASE_USERNAME=<db user>
DATABASE_PASSWORD=<db password>
APP_JWT_SECRET=<min 32 chars>
APP_ADMIN_EMAIL=<admin email>
APP_ADMIN_PASSWORD=<strong password>
MAIL_USERNAME=interiorarcstudio@gmail.com
MAIL_APP_PASSWORD=<16-digit Gmail App Password>
APP_STORAGE_BASE_URL=https://<your-railway-app>.railway.app/files
CORS_ALLOWED_ORIGINS=https://yourdomain.com,https://www.yourdomain.com
```

### 3. Verify

- API health: `GET http://localhost:8080/api/v1/projects`
- Swagger UI: `http://localhost:8080/swagger-ui/index.html`

### Environment Profiles

| Profile | Datasource | SQL Logging | Swagger |
|---|---|---|---|
| `dev` | `localhost:5432/portfolio_db` | ON | ON |
| `prod` | `$DATABASE_URL` env var | OFF | OFF |

### Java Version Note

If you have multiple JDKs, Maven must use JDK 17. OpenJDK 25 causes a Lombok `TypeTag::UNKNOWN` error. Set `JAVA_HOME` or configure it permanently in system environment variables.

---

## Roadmap

### ✓ Complete

- [x] JWT authentication for all `/admin/**` endpoints
- [x] React/Next.js frontend — public portfolio site + admin panel
- [x] Rate limiting: contact form 3/day per IP, login 5 failures/15min → block
- [x] Audit log system — async, immutable, `audit_logs` table (V3 migration)
- [x] SMTP email notification on contact form submit (async, HTML, spam detection)
- [x] CORS origin configured via `CORS_ALLOWED_ORIGINS` env var
- [x] Storage base URL configured via `APP_STORAGE_BASE_URL` env var
- [x] Input validation: tag/material item-level `@Size`, `detailedStory` capped at 50k chars
- [x] Security headers via Spring Security defaults (X-Frame-Options, X-Content-Type-Options)
- [x] Swagger disabled in production
- [x] ISR revalidation: `revalidate = 300` + on-demand via `POST /api/revalidate` on admin mutations

### Pending

- [ ] `S3StorageService` — toggle with `app.storage.type=s3`
- [ ] `MediaController` wired to audit log (MEDIA_UPLOADED / MEDIA_DELETED actions defined, not connected)
- [ ] Comment system — `comments` table in schema, entity pending
- [ ] Structured JSON logging for production (Logstash encoder)
