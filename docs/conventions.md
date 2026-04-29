# Backend Coding Conventions

Coding standards and architectural patterns for `ai-learning-platform-be` (Java 21 / Spring Boot 3.4.4).

## Package Structure
```
src/main/java/com/ailearning/platform/
├── ai/           AI engines (mastery, adaptive, spaced repetition, question generation)
├── config/       SecurityConfig, CorsConfig, RedisConfig, WebSocketConfig, JacksonConfig
├── controller/   REST controllers — thin, delegate to service layer
├── dto/
│   ├── request/  Incoming payloads (*Request suffix)
│   └── response/ Outgoing shapes (*Response suffix) — never expose entity
├── entity/       JPA entities
│   └── enums/    All enum types
├── exception/    GlobalExceptionHandler, ResourceNotFoundException
├── repository/   Spring Data JPA + custom JPQL queries
├── service/      Service interfaces
│   └── impl/     Service implementations
└── websocket/    STOMP message handlers
```

## Entity Conventions
- UUIDs for all PKs: `@GeneratedValue(strategy = GenerationType.UUID)`
- Timestamps: `@CreationTimestamp createdAt`, `@UpdateTimestamp updatedAt` on every entity
- Lombok: `@Builder @Getter @Setter @NoArgsConstructor @AllArgsConstructor` on all entities
- Relationships: `@ManyToOne(fetch = FetchType.LAZY)` by default; cascade deliberately
- Enums: always `@Enumerated(EnumType.STRING)` — never ordinal

## Service Layer
- **Interface + Impl**: `CourseService` → `CourseServiceImpl`
- Class-level `@Transactional(readOnly = true)` — read-only by default
- Override with `@Transactional` on write methods
- **Nested service calls**: if an inner service method may throw and fail independently (e.g. `EnrollmentService.updateProgress`), mark it `@Transactional(propagation = Propagation.REQUIRES_NEW)` to prevent poisoning the outer transaction
- Business logic lives in the service layer — controllers are thin
- Manual DTO mapping in service layer (no MapStruct)

## Lazy Loading Rules (`open-in-view: false`)
- Any method that traverses lazy associations MUST be `@Transactional` (or `readOnly=true`)
- For bulk queries that need associations, use JPQL `JOIN FETCH` — avoids N+1 and LazyInitializationException
  ```java
  @Query("SELECT ucp FROM UserConceptProgress ucp " +
         "JOIN FETCH ucp.concept c JOIN FETCH c.topic t JOIN FETCH t.module m JOIN FETCH m.course " +
         "WHERE ucp.user.id = :userId ...")
  ```

## Controller Conventions
- Thin: validate input (`@Valid`), delegate to service, return `ResponseEntity`
- `@PreAuthorize` for role-based access: `hasRole("ADMIN")`, `hasAnyRole("INSTRUCTOR", "ADMIN")`
- DTOs for all request/response — never return entity objects
- HTTP semantics: GET (read), POST (create/action), PUT (full update), DELETE (remove)
- All endpoints under `/api/`; public endpoints under `/api/public/`

## Naming
| Thing | Convention | Example |
|---|---|---|
| Packages | lowercase | `com.ailearning.platform.service` |
| Classes | PascalCase | `AssessmentServiceImpl` |
| Methods/fields | camelCase | `getMasteryLevel()` |
| Constants | UPPER_SNAKE_CASE | `MAX_HINT_LEVEL` |
| Request DTOs | `*Request` | `SubmitAnswerRequest` |
| Response DTOs | `*Response` | `AnswerResultResponse` |
| Repository methods | `findBy*`, `existsBy*`, `countBy*` | `findByUserIdAndConceptId` |
| REST paths | kebab-case | `/api/public/auth/login` |

## Authentication
- JWT: Nimbus JOSE `MACSigner`/`MACVerifier`, HMAC-SHA256, 24hr expiry
- Subject = user UUID; claims include `roles`, `username`, `email`, `displayName`
- `JwtAuthenticationConverter` maps `roles` claim → `ROLE_` Spring Security authorities
- BCrypt for password hashing
- CORS: configured in `CorsConfig`; `CORS_ORIGINS` env var (must include FE URL)

## Database
- Flyway migrations: `src/main/resources/db/migration/V{n}__{description}.sql`
- `spring.jpa.open-in-view=false` — explicit session management
- `spring.jpa.ddl-auto=update` — Hibernate supplements Flyway (handles new columns from entities)
- PostgreSQL-specific: JSONB (`@JdbcTypeCode(SqlTypes.JSON)`) for flexible content storage

## DTO ↔ Entity Field Mapping Gotchas
| Entity field | DTO field | Note |
|---|---|---|
| `LearningUnit.type` | `contentType` | intentional name mismatch |
| `Course.createdBy.displayName` | `createdByName` | NOT `instructorName` |
| `Course.estimatedDurationMinutes` | `estimatedDurationMinutes` | NOT hours |
| `Course.industryVertical` | `industryVertical` | displayed as "category" in FE |
| `Question.generatedForUserId` | `generatedForUserId` | NULL = shared question pool |

## Testing
- No automated test suite — all verification is manual via deployed API
- Test users: `testuser1`/`testpass123` (STUDENT), `testuser2`/`testpass123` (STUDENT)

## See also
- [api-contracts.md](api-contracts.md) — Full endpoint reference
- [progress.md](progress.md) — Development timeline
