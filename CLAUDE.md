# AI Learning Platform — Backend

## Project Overview
AI-native adaptive learning platform backend. Spring Boot REST API with AI tutoring, adaptive assessments, gamification, and real-time WebSocket support.

## Tech Stack
- **Java 21**, **Spring Boot 3.4.4**, **Maven**
- **PostgreSQL 16** (Flyway migrations), **Redis 7** (caching)
- **Keycloak 26** OAuth2/JWT auth (resource server)
- **OpenAI Java SDK 4.31.0** (GPT-4o)
- **Stripe** payments, **WebSocket/STOMP** real-time
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
- **Public endpoints**: `/api/public/**`, `GET /api/courses/**`, `/actuator/health`, `/ws/**`
- **Authenticated**: all other endpoints require JWT Bearer token
- **Admin**: `/api/admin/**` requires `ROLE_ADMIN`
- `/api/public/seed` — POST endpoint to seed demo data (idempotent)

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
| `OPENAI_API_KEY` | — | OpenAI API key |
| `OPENAI_MODEL` | gpt-4o | Model name |
| `STRIPE_API_KEY` | — | Stripe secret key |
| `CORS_ORIGINS` | http://localhost:5173 | Allowed CORS origins (comma-separated) |
| `KEYCLOAK_JWK_URI` | http://localhost:8180/realms/ai-learning/... | JWT verification endpoint |

## Conventions
- Entities use `@Builder`, `@Getter`, `@Setter`, `@NoArgsConstructor`, `@AllArgsConstructor` (Lombok)
- UUIDs for all entity IDs (`GenerationType.UUID`)
- `@CreationTimestamp` / `@UpdateTimestamp` on all entities
- Services are interface + impl pattern
- DTOs are separate request/response records
- `@Transactional(readOnly = true)` on service class, `@Transactional` on write methods
- `@Cacheable` / `@CacheEvict` for Redis caching on course reads/writes
- `findByPublishedTrue()` — courses must have `published=true` to appear in listings
