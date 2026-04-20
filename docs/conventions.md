# Backend Coding Conventions

Coding standards and architectural patterns for the AI Learning Platform backend (Java 21 / Spring Boot 3.4.4 / Maven).

## Project Structure
```
src/main/java/com/ailearning/platform/
├── config/        # SecurityConfig, CacheConfig, CorsConfig, RedisConfig, OpenAIConfig
├── controller/    # REST controllers (thin, delegate to service)
├── dto/           # Request/Response records, field-mapped from entities
├── exception/     # GlobalExceptionHandler, custom exceptions
├── model/         # JPA entities + enums
├── repository/    # Spring Data JPA repositories
├── security/      # JWT filter, JwtService, UserPrincipal
├── service/       # Interface + Impl pattern for all services
└── util/          # Shared utilities
```

## Entity Conventions
- **IDs**: `UUID` with `@GeneratedValue(strategy = GenerationType.UUID)` — never auto-increment
- **Timestamps**: `@CreationTimestamp` on `createdAt`, `@UpdateTimestamp` on `updatedAt`
- **Lombok**: `@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor` on all entities
- **Relationships**: `@ManyToOne(fetch = FetchType.LAZY)` by default; cascade carefully
- **Field naming**: Java camelCase → DB snake_case via Hibernate naming strategy (e.g., entity `contentType` → DB `content_type`)
- **Enums**: `@Enumerated(EnumType.STRING)` always

## Service Layer
- **Interface + Impl pattern**: `CourseService` interface → `CourseServiceImpl` class
- **Class-level**: `@Transactional(readOnly = true)` — read-only by default
- **Write methods**: Override with `@Transactional` (no readOnly)
- **Redis caching**: `@Cacheable("cacheName")` on reads, `@CacheEvict(value = "cacheName", allEntries = true)` on writes
- **Business logic**: All in service layer, controllers are thin delegation
- **Manual DTO mapping** in service layer (no MapStruct despite initial plan)

## Controller Conventions
- Thin controllers: validate input (`@Valid`), delegate to service, return `ResponseEntity`
- `@PreAuthorize` for role-based access: `hasRole("ADMIN")`, `hasAnyRole("INSTRUCTOR", "ADMIN")`
- DTOs for all request/response — never expose entities directly
- Standard HTTP methods: GET (read), POST (create/action), PUT (update), DELETE (remove)
- All endpoints under `/api/`

## Naming
- **Packages**: all lowercase (`com.ailearning.platform`)
- **Classes**: PascalCase (`CourseServiceImpl`, `AssessmentController`)
- **Methods/fields**: camelCase
- **Constants**: UPPER_SNAKE_CASE
- **DTOs**: `*Request` for input, `*Response` for output (records preferred)
- **Repository methods**: `findBy*`, `existsBy*`, `deleteBy*` (Spring Data query derivation)
- **Boolean repo**: `existsByUsernameIgnoreCase(String username)` pattern
- **REST endpoints**: kebab-case (e.g., `/api/public/auth/login`)

## Authentication & Security
- JWT: Nimbus JOSE `MACSigner`/`MACVerifier` with HMAC-SHA256, 24hr expiry
- `JwtAuthenticationFilter` extracts JWT, sets `SecurityContextHolder`
- `UserPrincipal` implements `UserDetails`, carries userId + roles
- BCrypt for password hashing (`BCryptPasswordEncoder` bean)
- CORS: configured origins (localhost:5173, Vercel domain)

## Database
- PostgreSQL 16, Hibernate DDL `update` mode
- `spring.jpa.open-in-view=false` — explicit fetching
- Connection pool via HikariCP (Railway-provided URL)
- Key enums:
  - `QuestionType`: MCQ, CODING, SUBJECTIVE, SCENARIO_BASED
  - `CourseStatus`: DRAFT, PENDING_APPROVAL, PUBLISHED, CHANGES_REQUESTED
  - `UserRole`: STUDENT, PENDING_INSTRUCTOR, INSTRUCTOR, ADMIN, ENTERPRISE_ADMIN
- Redis 7 for caching (course lists, leaderboard)

## DTO ↔ Entity Field Mapping Gotchas
| Entity Field | DTO Field | Notes |
|---|---|---|
| `LearningUnit.type` | `contentType` | Name mismatch — intentional |
| `Course.createdBy.displayName` | `createdByName` | NOT `instructorName` |
| `Course.estimatedDurationMinutes` | `estimatedDurationMinutes` | NOT hours |
| `Course.industryVertical` | `industryVertical` | Used as category on frontend |

## Testing
- No automated test suite — all verification is manual via deployed API
- Test users: `testuser1`/`testpass123` (STUDENT), `admin`/`admin123` (ADMIN)

## See also
- [api-contracts.md](api-contracts.md) — Full endpoint reference
- [progress.md](progress.md) — Development timeline