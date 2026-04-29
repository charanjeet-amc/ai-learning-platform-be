# AI Learning Platform — Backend

## Product Vision
AI-native adaptive learning platform. Surpass Coursera/Udemy/DeepLearning.AI — not video courses with AI bolted on.

## Tech Stack
- **Java 21**, **Spring Boot 3.4.4**, **Maven**
- **PostgreSQL 16** (Flyway migrations in `src/main/resources/db/migration/`)
- **Redis 7** (`@Cacheable`/`@CacheEvict` on course reads/writes)
- **OpenAI Java SDK 4.31.0** (GPT-4o) — AI Tutor + AI answer evaluation
- **Nimbus JOSE JWT** (HMAC-SHA256), **Lombok**, **Hibernate**
- **Stripe** (wired, not active), **WebSocket/STOMP** config (exists, not production-active)

## Build & Deploy
```bash
mvn clean package -DskipTests   # build JAR
railway up --detach              # deploy (always --detach; NOT redeploy)
railway deployment list          # check SUCCESS/FAILED/BUILDING
railway logs                     # tail logs
```
Live URL: `https://ai-learning-platform-be-production.up.railway.app`

## Source Layout
```
src/main/java/com/ailearning/platform/
├── ai/           MasteryCalculator, AdaptiveEngine, SpacedRepetitionEngine, QuestionGeneratorEngine
├── config/       SecurityConfig, CorsConfig, RedisConfig, WebSocketConfig, JacksonConfig
├── controller/   One per domain (thin — delegates to service)
├── dto/
│   ├── request/  Incoming payloads
│   └── response/ Outgoing shapes (never expose entity directly)
├── entity/       JPA entities
│   └── enums/    All enum types
├── exception/    GlobalExceptionHandler, ResourceNotFoundException
├── repository/   Spring Data JPA + JPQL queries
├── service/      Interfaces
│   └── impl/     Implementations
└── websocket/    STOMP handlers
```

## Security
| Pattern | Access |
|---|---|
| `/api/public/**` | Anonymous |
| `GET /api/courses/**` | Anonymous |
| `/api/admin/**` | `ROLE_ADMIN` |
| `/api/instructor/**` | `ROLE_INSTRUCTOR` or `ROLE_ADMIN` |
| All other `/api/**` | Authenticated (valid JWT) |

JWT subject = user UUID. `roles` claim → `ROLE_` Spring Security authorities via `JwtAuthenticationConverter`.

## Conventions
See `docs/conventions.md` for full details. Key rules:
- **Entities**: `@Builder @Getter @Setter @NoArgsConstructor @AllArgsConstructor`; UUID PKs
- **Services**: interface + impl; `@Transactional(readOnly=true)` at class level; `@Transactional` on writes
- **Nested transactions**: Use `Propagation.REQUIRES_NEW` when a called service may fail independently (e.g. `EnrollmentService.updateProgress`) — prevents poisoning the outer transaction
- **Lazy loading + `open-in-view: false`**: Every service method that traverses lazy associations must be `@Transactional`; use `JOIN FETCH` in JPQL for bulk queries
- **DTOs**: Never expose entities directly; always map to response DTOs
- **Redis**: `@Cacheable("courses")` on reads; `@CacheEvict(allEntries=true)` on writes

## Enums (shared with FE — never rename without updating `src/types/index.ts`)
| Enum | Values |
|---|---|
| `DifficultyLevel` | BEGINNER, EASY, MEDIUM, HARD, ADVANCED |
| `ContentType` | TEXT, VIDEO, INTERACTIVE, CODE_EXERCISE, DIAGRAM, QUIZ, SIMULATION, AUDIO |
| `ConceptStatus` | NOT_STARTED, IN_PROGRESS, STRUGGLING, MASTERED, REVIEW_NEEDED |
| `CourseStatus` | DRAFT → PENDING_APPROVAL → PUBLISHED / CHANGES_REQUESTED |
| `UserRole` | STUDENT, PENDING_INSTRUCTOR, INSTRUCTOR, ADMIN, ENTERPRISE_ADMIN |
| `QuestionType` | MCQ, CODING, SUBJECTIVE, SCENARIO_BASED |
| `LearningStyle` | VISUAL, READING, KINESTHETIC, AUDITORY |
| `ApplicationStatus` | PENDING, UNDER_REVIEW, APPROVED, REJECTED |

## DB Column Quirks
| Entity field | DB column | Note |
|---|---|---|
| `User.fullName` | `display_name` | |
| `Course.estimatedDurationMinutes` | `estimated_hours` | field name is minutes, column is hours |
| `Course.createdBy` | `instructor_id` | FK |
| `Concept.definition` | `description` | |
| `LearningUnit.type` | `content_type` | DTO uses `contentType` |
| `Question.generatedForUserId` | `generated_for_user_id` | NULL = shared; non-NULL = user-specific AI question |

## Flyway Migrations
| Version | Purpose |
|---|---|
| V1 | Full initial schema |
| V2 | Sample course/module/topic/concept seed content |
| V3 | Certificates table |
| V4 | Fix enrollment `completed` column |
| V5 | Add `generated_for_user_id` to `questions` (AI question user-scoping) |
| V6 | Delete orphaned AI questions created before V5 (had no user scope) |

**Next migration: V7.**

## AI Components
- **MasteryCalculator**: weighted formula (score + recency + hints + confidence) → 0.0–1.0
- **AdaptiveEngine**: selects next concept; returns `nextAction` = advance / reinforce / remediate
- **SpacedRepetitionEngine**: SM-2; sets `nextReviewAt` on mastered concepts for review queue
- **QuestionGeneratorEngine**: GPT-4o generates 3 questions per call; saved with `generatedForUserId`
- **AI Tutor**: Socratic method, 4-level hint escalation; hidden during active quiz (anti-cheat)

## Environment Variables
| Variable | Description |
|---|---|
| `DB_HOST/PORT/NAME/USERNAME/PASSWORD` | PostgreSQL connection |
| `REDIS_HOST/REDIS_PORT` | Redis connection |
| `OPENAI_API_KEY` | OpenAI (GPT-4o) |
| `JWT_SECRET` | HMAC-SHA256 signing key |
| `JWT_EXPIRATION_HOURS` | Default 24 |
| `CORS_ORIGINS` | Comma-separated allowed origins — must include FE URL |
| `STRIPE_API_KEY` | Stripe (not yet active) |

## Features Not Yet Implemented
- XP-based level/tier progression
- Pre-assessment diagnostic fast-track
- Stripe payment flow
- Pinecone/RAG for AI tutor context
- WebSocket AI tutor streaming (currently HTTP POST)

---

## Shared Context — API Contracts & Cross-Layer Rules

> Full endpoint reference: `docs/api-contracts.md`

### Critical Field Mappings (BE → FE)
| Backend DTO field | Frontend field | Pitfall |
|---|---|---|
| `AITutorRequest.query` | `request.query` | NEVER `message` |
| `AITutorResponse.message` | `response.message` | NEVER `response` |
| `LearningUnit` entity `.type` → DTO `contentType` | `contentType` | entity field name differs |
| `Course.estimatedDurationMinutes` | `estimatedDurationMinutes` | NEVER `estimatedHours` |
| `Course.createdBy.displayName` → `createdByName` | `createdByName` | NOT `instructorName` |
| `AnswerResultResponse.score` | `result.score` | float 0.0–1.0 |
| `AnswerResultResponse.feedback` | `result.feedback` | AI feedback string |
| `UserProgressResponse.courseId` | `ReviewItem.courseId` | required for review-queue navigation links |

### Cross-Cutting Rules
1. **XP awarded server-side only** — FE displays, never computes
2. **AI Tutor hidden during quiz** — enforced in `CoursePlayerPage`; prevents answer copying
3. **Learning graph order**: Course → Module → Topic → Concept → LearningUnit (canonical everywhere)
4. `nextConceptId` = AI recommendation (may skip mastered); `steps` = all concepts (for stable progress %)
5. **Content JSONB format**: `{"body": "..."}` — `ContentViewer` checks `body`, `markdown`, `text` keys
6. **`VITE_API_URL` (FE) must match `CORS_ORIGINS` (BE)** on every environment
7. **Enum or field rename = breaking change** — update BE + FE types + docs in one commit
8. **AI questions are user-scoped**: `generated_for_user_id` NULL = shared; non-NULL = only that user sees it
