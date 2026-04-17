# AI Learning Platform — Backend

## Product Vision
Build the most advanced AI-powered learning platform that surpasses Coursera, Udemy, and DeepLearning.AI. AI-native adaptive learning — not video courses with AI bolted on.

## Core Requirements
1. **Knowledge Graph Architecture**: Courses → Modules → Topics → Concepts → LearningUnits. Concept dependencies form a DAG for optimal learning order.
2. **AI Adaptive Learning**: Personalized paths per student. Concept mastery tracking (NOT completion-based). Adaptive difficulty. Spaced repetition. Statuses: NOT_STARTED → IN_PROGRESS → STRUGGLING / MASTERED / REVIEW_NEEDED.
3. **AI Tutor (GPT-4o)**: Real-time conversational tutor via WebSocket. Context-aware, Socratic method, detects misconceptions.
4. **Adaptive Assessments**: MCQ, code exercises, short answer, etc. AI generates questions dynamically. Difficulty adjusts mid-assessment.
5. **Gamification**: XP points, daily streaks with multipliers, badges, leaderboard, levels.
6. **Roles & Auth**: Student, Pending Instructor, Instructor, Admin, Enterprise Admin. Self-issued HMAC-SHA256 JWT. Subscription tiers: FREE, BASIC, PRO, ENTERPRISE.
7. **Instructor Onboarding**: Register as PENDING_INSTRUCTOR → submit application (profile, links, experience) → admin review → approve/reject → only INSTRUCTOR can create courses.
8. **Payments**: Stripe integration for course purchases and subscriptions.
9. **Real-time**: WebSocket/STOMP for AI tutor chat, notifications.

## Project Overview
Spring Boot REST API implementing all of the above.

## Tech Stack
- **Java 21**, **Spring Boot 3.4.4**, **Maven**
- **PostgreSQL 16** (Flyway migrations), **Redis 7** (caching)
- **Self-issued HMAC-SHA256 JWT** auth (replaced Keycloak — no Keycloak server needed)
- **OpenAI Java SDK 4.31.0** (GPT-4o) — AI Tutor with Socratic method + AI-powered answer evaluation
- **Stripe** payments (wired, not yet active), **WebSocket/STOMP** real-time (config exists)
- **Lombok**, **MapStruct**, **Hibernate**

## Build & Run
```bash
mvn clean compile          # compile
mvn clean package -DskipTests  # build JAR
mvn spring-boot:run        # run locally (needs Postgres + Redis)
```

## Deployment
- **Railway** (Docker): `railway up --detach` (NOT `railway redeploy` — that reuses old images)
- Dockerfile: multi-stage (`maven:3.9-eclipse-temurin-21-alpine` → `eclipse-temurin:21-jre-alpine`)
- Backend URL: `https://ai-learning-platform-be-production.up.railway.app`

## Project Structure
```
src/main/java/com/ailearning/platform/
├── ai/                    # OpenAI integration (tutor, content generation)
├── config/                # SecurityConfig (JwtAuthenticationConverter), CorsConfig, RedisConfig, WebSocketConfig, JacksonConfig
├── controller/
│   ├── AdminCourseController.java       # /api/admin/courses (approve, reject, edit any course)
│   ├── AdminInstructorController.java   # GET/POST /api/admin/instructor-applications (list, detail, approve, reject)
│   ├── AITutorController.java           # POST /api/ai-tutor
│   ├── AssessmentController.java        # /api/assessments
│   ├── AuthController.java              # /api/public/auth (register, login, register-instructor)
│   ├── CourseController.java            # /api/courses (CRUD, @PreAuthorize on create/update/publish)
│   ├── DashboardController.java         # /api/dashboard
│   ├── EnrollmentController.java        # /api/enrollments
│   ├── GamificationController.java      # /api/gamification
│   ├── InstructorApplicationController.java  # /api/instructor-application (submit/view)
│   ├── InstructorController.java        # /api/instructor
│   ├── LearningHistoryController.java   # /api/learning-history
│   ├── LearningPathController.java      # /api/learning-paths
│   ├── OAuth2Controller.java            # /api/public/auth/oauth2
│   ├── SeedController.java              # /api/public/seed (with category backfill) + /seed-extra-questions
│   └── UserProfileController.java       # /api/users/me
├── dto/
│   ├── request/           # Incoming DTOs
│   └── response/          # Outgoing DTOs (incl. AnswerResultResponse with score + feedback)
├── entity/                # JPA entities (26 entities)
│   └── enums/             # DifficultyLevel, ContentType, ConceptStatus, UserRole, ApplicationStatus, CourseStatus, QuestionType, etc.
├── exception/             # GlobalExceptionHandler, ResourceNotFoundException
├── mapper/                # MapStruct mappers
├── repository/            # Spring Data JPA repositories (20 repos)
├── service/               # Service interfaces
│   └── impl/              # Service implementations
│       └── AssessmentServiceImpl.java  # AI-powered evaluation (GPT-4o) for SUBJECTIVE/CODING
└── websocket/             # WebSocket message handlers
```

## Key Entities & Relationships
```
User → Enrollment → Course → Module → Topic → Concept → LearningUnit
                                                      → Question
                                                      → ConceptMisconception
                                                      → ConceptSocratic
                                                      → ConceptOutcome
User → UserLearningProfile, UserConceptProgress, UserWeakArea
User → XPEvent, UserBadge → Badge
Course → LearningPath → LearningStep
User → AIInteraction, UserAttempt, Payment, Notification
User → InstructorApplication (OneToOne)
```

## Database
- **Flyway migrations**: `src/main/resources/db/migration/`
  - `V1__init_schema.sql` — full schema with PostgreSQL enums, indexes, constraints
  - `V2__seed_courses.sql` — seed data (5 courses with modules/topics/concepts)
- **JPA ddl-auto**: `update` (Hibernate supplements Flyway)
- Important column mappings (entity field → DB column):
  - `User.fullName` → `display_name`
  - `User.lastActiveAt` → `last_active_date`
  - `Course.estimatedDurationMinutes` → `estimated_hours`
  - `Course.createdBy` → `instructor_id` (FK)
  - `Concept.definition` → `description`
  - `Concept.difficultyLevel` → `difficulty`
  - `LearningUnit.type` → `content_type`
  - `Question.type` → `question_type`
- Course entity has `@PrePersist` auto-generating `slug` from `title`

## Security
- **Auth**: Self-issued HMAC-SHA256 JWT (Nimbus JOSE library from spring-boot-starter-oauth2-resource-server)
- **JwtTokenProvider** (`config/JwtTokenProvider.java`): generates tokens with userId (subject), username, email, displayName, roles claims
- **SecurityConfig** (`config/SecurityConfig.java`): NimbusJwtDecoder with SecretKeySpec, BCryptPasswordEncoder
- **JwtAuthenticationConverter**: Maps JWT `roles` claim to `ROLE_` Spring Security authorities (required for hasRole() and @PreAuthorize)
- **@EnableMethodSecurity**: Enables `@PreAuthorize` on controller methods
- **AuthController** (`controller/AuthController.java`):
  - `POST /api/public/auth/register` — creates STUDENT user, returns JWT + user info
  - `POST /api/public/auth/login` — accepts username OR email, validates BCrypt password, returns JWT
  - `POST /api/public/auth/register-instructor` — creates PENDING_INSTRUCTOR user, returns JWT
  - Duplicate email/username returns 409 Conflict
- **InstructorApplicationController** (`controller/InstructorApplicationController.java`):
  - `GET /api/instructor-application` — get current user's application
  - `POST /api/instructor-application` — submit/update application (validates PENDING_INSTRUCTOR role)
- **AdminInstructorController** (`controller/AdminInstructorController.java`):
  - `GET /api/admin/instructor-applications?status=PENDING` — list by status
  - `GET /api/admin/instructor-applications/{id}` — application detail
  - `POST /api/admin/instructor-applications/{id}/approve` — approve, upgrade role to INSTRUCTOR
  - `POST /api/admin/instructor-applications/{id}/reject` — reject with optional notes
- **Public endpoints**: `/api/public/**`, `GET /api/courses/**`, `/actuator/health`, `/ws/**`
- **Authenticated**: all other endpoints require `Authorization: Bearer <jwt>` header
- **Admin**: `/api/admin/**` requires `hasRole("ADMIN")` (instructor review + course management)
- **Instructor guard**: `@PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")` on CourseController create/update/publish
- JWT subject = user UUID (used by `@AuthenticationPrincipal Jwt jwt` → `jwt.getSubject()`)
- `/api/public/seed` — POST endpoint to seed demo data (idempotent)
- **Test users in DB**: testuser1/testpass123, testuser2/testpass123

### Auth DTOs
- `RegisterRequest`: username (3-50 chars), email, password (6-100 chars), displayName
- `LoginRequest`: username, password
- `AuthResponse`: token, userId, username, email, displayName, avatarUrl, roles

### User Entity Auth Fields
- `passwordHash` (String, nullable — seed users don't have passwords)
- `keycloakId` (String, nullable, unique — set to "local-"+UUID for locally registered users)

## Enums (Java ↔ PostgreSQL)
- `DifficultyLevel`: BEGINNER, EASY, MEDIUM, HARD, ADVANCED
- `ContentType`: TEXT, VIDEO, INTERACTIVE, CODE_EXERCISE, DIAGRAM, QUIZ, SIMULATION, AUDIO
- `ConceptStatus`: NOT_STARTED, IN_PROGRESS, STRUGGLING, MASTERED, REVIEW_NEEDED
- `UserRole`: STUDENT, PENDING_INSTRUCTOR, INSTRUCTOR, ADMIN, ENTERPRISE_ADMIN
- `ApplicationStatus`: PENDING, UNDER_REVIEW, APPROVED, REJECTED
- `CourseStatus`: DRAFT, PENDING_APPROVAL, PUBLISHED, CHANGES_REQUESTED
- `QuestionType`: MCQ, CODING, SUBJECTIVE, SCENARIO_BASED

## Environment Variables
| Variable | Default | Description |
|---|---|---|
| `DB_HOST` | localhost | PostgreSQL host |
| `DB_PORT` | 5432 | PostgreSQL port |
| `DB_NAME` | ai_learning_platform | Database name |
| `DB_USERNAME` | postgres | DB user |
| `DB_PASSWORD` | postgres | DB password |
| `REDIS_HOST` | localhost | Redis host |
| `REDIS_PORT` | 6379 | Redis port |
| `OPENAI_API_KEY` | — | OpenAI API key (set on Railway) |
| `OPENAI_MODEL` | gpt-4o | Model name |
| `STRIPE_API_KEY` | — | Stripe secret key |
| `CORS_ORIGINS` | http://localhost:5173 | Allowed CORS origins (comma-separated) |
| `JWT_SECRET` | aiLearningPlatform...Signing | HMAC-SHA256 signing key |
| `JWT_EXPIRATION_HOURS` | 24 | JWT token validity |

## Conventions
- Entities use `@Builder`, `@Getter`, `@Setter`, `@NoArgsConstructor`, `@AllArgsConstructor` (Lombok)
- UUIDs for all entity IDs (`GenerationType.UUID`)
- `@CreationTimestamp` / `@UpdateTimestamp` on all entities
- Services are interface + impl pattern
- DTOs are separate request/response records
- `@Transactional(readOnly = true)` on service class, `@Transactional` on write methods
- `@Cacheable` / `@CacheEvict` for Redis caching on course reads/writes
- `findByPublishedTrue()` — courses must have `published=true` to appear in listings

## Current Status (Updated April 17, 2026)
- **LIVE** on Railway — all endpoints working
- **5 courses seeded** with full knowledge graph (modules → topics → concepts → learning units, 61+ questions, categories)
- **Auth working**: Self-issued JWT auth — register, login, logout, register-instructor
- **JWT roles mapped**: JwtAuthenticationConverter maps `roles` claim to `ROLE_` Spring Security authorities
- **AI Tutor working**: GPT-4o Socratic method, context-aware, hint escalation (levels 1-4)
- **AI-powered answer evaluation**: GPT-4o evaluates SUBJECTIVE/CODING answers semantically with score (0-1) + detailed feedback; falls back to keyword matching if API fails
- **Multiple question types**: MCQ, CODING, SUBJECTIVE, SCENARIO_BASED with type-specific evaluation
- **Enrollment working**: Enroll, unenroll, enrollment status check, progress tracking, completion
- **Gamification working**: XP (10 enrollment, 10 correct answer, 50 mastery, 2 AI), streaks, badges, leaderboard
- **Adaptive engine working**: MasteryCalculator (M=0.4S+0.2C+0.2R+0.2T), AdaptiveEngine (advance/reinforce/remediate), SpacedRepetitionEngine (SM-2)
- **Assessment working**: Quiz endpoints, multiple question types, XP on correct, adaptive difficulty, AI evaluation
- **Instructor onboarding working**: Register as PENDING_INSTRUCTOR → submit application → admin review → approve/reject
- **Instructor course CRUD working**: Create/update/publish (guarded by @PreAuthorize), DOCX import, Cloudinary upload
- **Course approval workflow**: DRAFT → PENDING_APPROVAL → PUBLISHED / CHANGES_REQUESTED (AdminCourseController)
- **Admin features**: Instructor review + course approval/rejection + edit any course
- **Course catalog filtering working**: GET /api/courses/filter (search, category, difficulty, duration), GET /api/courses/categories
- **Dashboard working**: Enrolled courses, weak areas, review queue, badges, XP, rank
- **Learning history working**: Per-course progress, recent activity feed, timezone-correct timestamps
- **Profile/Settings working**: GET/PUT /api/users/me, change password, delete account

## Important Field Mappings (Backend DTO ↔ Frontend)
- `LearningUnitResponse.contentType` (entity field is `type` — mapped in `CourseServiceImpl`)
- `AITutorRequest.query` (frontend was sending `message` — fixed)
- `AITutorResponse.message` (frontend was reading `response` — fixed)
- `AITutorResponse.sessionId` — auto-generated UUID if client doesn't send one
- Seed data stores content as `{"body": "..."}` in JSONB
- `User.fullName` → `display_name` DB column
- `User.keycloakId` repurposed as bio field for local auth users

## Bugs Fixed (April 13, 2026)
1. `prerequisites` TEXT mismatch — entity/DTOs changed to `String`
2. GlobalExceptionHandler was swallowing errors — now logs and returns actual messages
3. `Enrollment.enrolledAt` null on UPDATE — added `@Column(updatable=false)` + explicit `LocalDateTime.now()`
4. `learningUnits` missing from tree API — `mapConceptResponse()` wasn't mapping them; added `mapLearningUnitResponse()`
5. AI Tutor `sessionId` null on save — DB has NOT NULL constraint; now auto-generates UUID
6. AI Tutor field mismatches with frontend — aligned request/response field names

## Bugs Fixed (April 14, 2026)
7. Save concept failing — removed `@NotNull`/`@NotBlank` from `CreateConceptRequest` (update sends no topicId)
8. Timestamps wrong timezone — added `JacksonConfig.java` to append 'Z' to LocalDateTime serialization (Railway JVM=UTC)

## Files Added/Modified (April 14)
- `config/JacksonConfig.java` — NEW: serializes LocalDateTime with 'Z' UTC suffix
- `controller/UserProfileController.java` — NEW: profile CRUD, change password, delete account
- `dto/request/CreateConceptRequest.java` — removed @NotNull/@NotBlank annotations

## Bugs Fixed (April 15, 2026)
9. JWT roles not mapped to Spring Security authorities → added JwtAuthenticationConverter bean
10. hasRole("ADMIN") / @PreAuthorize never matched → now maps JWT `roles` claim to `ROLE_` authorities
11. Categories missing on existing seed data → added backfill logic in SeedController "already seeded" block
12. Instant instructor role upgrade (no vetting) → replaced with PENDING_INSTRUCTOR → application → admin approval flow

## Files Added/Modified (April 15)
- `config/SecurityConfig.java` — MODIFIED: added JwtAuthenticationConverter, @EnableMethodSecurity, maps roles claim to ROLE_ authorities
- `controller/AuthController.java` — MODIFIED: added POST /api/public/auth/register-instructor endpoint
- `controller/CourseController.java` — MODIFIED: added @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')") on create/update/publish
- `controller/AdminInstructorController.java` — NEW: admin review of instructor applications (list, detail, approve, reject)
- `controller/InstructorApplicationController.java` — NEW: submit/view instructor application
- `controller/SeedController.java` — MODIFIED: category backfill for existing courses
- `entity/InstructorApplication.java` — NEW: OneToOne with User, all profile fields (headline, cvUrl, linkedinUrl, githubUrl, websiteUrl, yearsTeaching, currentInstitution, teachingDescription, youtubeChannelUrl, youtubeSubscribers, otherPlatforms, expertise, whyTeach, status, adminNotes, reviewedBy, reviewedAt)
- `entity/enums/ApplicationStatus.java` — NEW: PENDING, UNDER_REVIEW, APPROVED, REJECTED
- `entity/enums/UserRole.java` — MODIFIED: added PENDING_INSTRUCTOR
- `repository/InstructorApplicationRepository.java` — NEW: findByUserId, findByStatusOrderByCreatedAtAsc, existsByUserId

## Features Not Yet Implemented
- XP-based levels/tier progression
- Pre-assessment fast-track (diagnostic at module start)
- Stripe payment flow
- Pinecone/RAG vector search for AI tutor context
- WebSocket for AI tutor streaming (currently HTTP POST; notification handlers exist)
