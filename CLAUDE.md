# AI Learning Platform — Backend

## Product Vision
Build the most advanced AI-powered learning platform that surpasses Coursera, Udemy, and DeepLearning.AI. AI-native adaptive learning — not video courses with AI bolted on.

## Core Requirements
1. **Knowledge Graph Architecture**: Courses → Modules → Topics → Concepts → LearningUnits. Concept dependencies form a DAG for optimal learning order.
2. **AI Adaptive Learning**: Personalized paths per student. Concept mastery tracking (NOT completion-based). Adaptive difficulty. Spaced repetition. Statuses: NOT_STARTED → IN_PROGRESS → STRUGGLING / MASTERED / REVIEW_NEEDED.
3. **AI Tutor (GPT-4o)**: Real-time conversational tutor via WebSocket. Context-aware, Socratic method, detects misconceptions.
4. **Adaptive Assessments**: MCQ, code exercises, short answer, etc. AI generates questions dynamically. Difficulty adjusts mid-assessment.
5. **Gamification**: XP points, daily streaks with multipliers, badges, leaderboard, levels.
6. **Roles & Auth**: Student, Instructor, Admin, Content Creator. Keycloak OAuth2/JWT. Subscription tiers: FREE, BASIC, PRO, ENTERPRISE.
7. **Payments**: Stripe integration for course purchases and subscriptions.
8. **Real-time**: WebSocket/STOMP for AI tutor chat, notifications.

## Project Overview
Spring Boot REST API implementing all of the above.

## Tech Stack
- **Java 21**, **Spring Boot 3.4.4**, **Maven**
- **PostgreSQL 16** (Flyway migrations), **Redis 7** (caching)
- **Self-issued HMAC-SHA256 JWT** auth (replaced Keycloak — no Keycloak server needed)
- **OpenAI Java SDK 4.31.0** (GPT-4o) — AI Tutor with Socratic method
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
├── config/                # SecurityConfig, CorsConfig, RedisConfig, WebSocketConfig
├── controller/            # REST controllers (Course, AITutor, Assessment, Dashboard, Enrollment, Gamification, Seed)
├── dto/
│   ├── request/           # Incoming DTOs
│   └── response/          # Outgoing DTOs
├── entity/                # JPA entities (24 entities)
│   └── enums/             # DifficultyLevel, ContentType, ConceptStatus, UserRole, etc.
├── exception/             # GlobalExceptionHandler, ResourceNotFoundException
├── mapper/                # MapStruct mappers
├── repository/            # Spring Data JPA repositories (19 repos)
├── service/               # Service interfaces
│   └── impl/              # Service implementations
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
- **AuthController** (`controller/AuthController.java`):
  - `POST /api/public/auth/register` — creates user, returns JWT + user info
  - `POST /api/public/auth/login` — accepts username OR email, validates BCrypt password, returns JWT
  - Duplicate email/username returns 409 Conflict
- **Public endpoints**: `/api/public/**`, `GET /api/courses/**`, `/actuator/health`, `/ws/**`
- **Authenticated**: all other endpoints require `Authorization: Bearer <jwt>` header
- **Admin**: `/api/admin/**` requires `ROLE_ADMIN`
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
- `UserRole`: STUDENT, INSTRUCTOR, ADMIN, CONTENT_CREATOR
- `QuestionType`: MULTIPLE_CHOICE, TRUE_FALSE, SHORT_ANSWER, CODE_COMPLETION, CODE_DEBUG, MATCHING, ORDER

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

## Current Status (Updated April 15, 2026)
- **LIVE** on Railway — all endpoints working
- **5 courses seeded** with full knowledge graph (modules → topics → concepts → learning units, 50+ questions)
- **Auth working**: Self-issued JWT auth — register, login, logout
- **AI Tutor working**: GPT-4o Socratic method, context-aware, hint escalation (levels 1-4)
- **Enrollment working**: Enroll, unenroll, enrollment status check, progress tracking, completion
- **Gamification working**: XP (10 enrollment, 10 correct answer, 50 mastery, 2 AI), streaks, badges, leaderboard
- **Adaptive engine working**: MasteryCalculator (M=0.4S+0.2C+0.2R+0.2T), AdaptiveEngine (advance/reinforce/remediate), SpacedRepetitionEngine (SM-2)
- **Assessment working**: Quiz endpoints, multiple question types, XP on correct, adaptive difficulty
- **Instructor working**: Course CRUD, DOCX import, module/topic/concept CRUD, Cloudinary media upload
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

## Features Not Yet Implemented
- Course catalog filter endpoint (difficulty/category/tags)
- XP-based levels/tier progression
- Pre-assessment fast-track (diagnostic at module start)
- Admin dashboard endpoints
- Stripe payment flow
- Pinecone/RAG vector search for AI tutor context
- WebSocket for AI tutor streaming (currently HTTP POST; notification handlers exist)
