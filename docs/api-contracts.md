# API Contracts

REST API contracts for the AI Learning Platform backend. This is the source of truth for endpoint structure, request/response shapes, and error handling.

## Base URL
- **Production**: `https://ai-learning-platform-be-production.up.railway.app`
- **Local dev**: `http://localhost:8080`
- All endpoints prefixed with `/api/`

## Authentication
- JWT Bearer token in `Authorization: Bearer <token>` header for all non-public endpoints
- Self-issued HMAC-SHA256 JWT (Nimbus JOSE), 24hr expiry
- JWT subject = user UUID; claims include `roles` list, `username`, `email`, `displayName`

## Security Tiers
| Tier | Pattern | Rule |
|------|---------|------|
| Public | `/api/public/**`, `GET /api/courses/**` | No auth required |
| Authenticated | All other `/api/**` | Valid JWT required |
| Admin | `/api/admin/**` | `hasRole("ADMIN")` |
| Instructor | Course create/update/publish | `@PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")` |

## Error Handling
| Code | Meaning | When |
|------|---------|------|
| 400 | Bad Request | Validation error (`@Valid` failures) with field details |
| 401 | Unauthorized | Missing/expired JWT |
| 403 | Forbidden | Insufficient role |
| 404 | Not Found | Resource doesn't exist |
| 409 | Conflict | Duplicate email/username on register |
| 500 | Internal Server Error | Unexpected backend failure |

---

## Auth Endpoints (`AuthController`)

### `POST /api/public/auth/register`
Creates STUDENT user, returns JWT.
```
Request:  { username (3-50), email, password (6-100), displayName }
Response: { token, userId, username, email, displayName, avatarUrl, roles }
```

### `POST /api/public/auth/login`
Accepts username OR email + password (BCrypt validated).
```
Request:  { username, password }
Response: { token, userId, username, email, displayName, avatarUrl, roles }
```

### `POST /api/public/auth/register-instructor`
Creates PENDING_INSTRUCTOR user.
```
Request:  { username, email, password, displayName }
Response: { token, userId, username, email, displayName, avatarUrl, roles: ["PENDING_INSTRUCTOR"] }
```

---

## Course Endpoints (`CourseController`)

### `GET /api/courses` — List published courses
### `GET /api/courses/{id}` — Course detail
### `GET /api/courses/{id}/tree` — Full tree (modules → topics → concepts → learningUnits)
### `GET /api/courses/filter?search=&category=&difficulty=&minDuration=&maxDuration=` — Filtered list
### `GET /api/courses/categories` — Distinct category list
### `POST /api/courses` — Create course (INSTRUCTOR/ADMIN)
### `PUT /api/courses/{id}` — Update course (INSTRUCTOR/ADMIN)
### `POST /api/courses/{id}/publish` — Publish course (INSTRUCTOR/ADMIN)

---

## AI Tutor (`AITutorController`)

### `POST /api/ai-tutor`
```
Request:  { courseId, conceptId, query, sessionId? }
Response: { message, sessionId, hintLevel }
```
- `query` (NOT `message`) — the student's question
- `sessionId` auto-generated if not provided
- Socratic method with 4-level hint escalation

---

## Assessment Endpoints (`AssessmentController`)

### `GET /api/assessments/questions?conceptId={id}` — Get questions for concept
### `POST /api/assessments/submit`
```
Request:  { questionId, answer, attemptId? }
Response: { attemptId, correct, score, explanation, feedback, updatedMastery, nextAction, xpEarned, nextConceptId }
```
- MCQ/SCENARIO_BASED: exact case-insensitive match
- SUBJECTIVE/CODING: AI-powered GPT-4o evaluation (score 0-1 + detailed feedback); keyword fallback if API fails

---

## Enrollment Endpoints (`EnrollmentController`)
- `POST /api/enrollments/{courseId}` — Enroll (awards 10 XP)
- `DELETE /api/enrollments/{courseId}` — Unenroll
- `GET /api/enrollments/{courseId}/status` — Enrollment status

## Dashboard (`DashboardController`)
- `GET /api/dashboard` — Enrolled courses, weak areas, review queue, badges, XP, rank

## Gamification (`GamificationController`)
- `GET /api/gamification/xp` — User XP total
- `GET /api/gamification/badges` — User badges
- `GET /api/gamification/leaderboard` — Global leaderboard

## Learning Path (`LearningPathController`)
- `GET /api/learning-paths/{courseId}` — Next concept, progress %

## Learning History (`LearningHistoryController`)
- `GET /api/learning-history` — Per-course progress, recent activity

## Profile (`UserProfileController`)
- `GET /api/users/me` — Current user profile
- `PUT /api/users/me` — Update profile (displayName, bio, avatar)
- `PUT /api/users/me/password` — Change password
- `DELETE /api/users/me` — Delete account

---

## Instructor Application (`InstructorApplicationController`)
- `GET /api/instructor-application` — Get own application
- `POST /api/instructor-application` — Submit/update application

## Admin Instructor Review (`AdminInstructorController`)
- `GET /api/admin/instructor-applications?status=PENDING` — List by status
- `GET /api/admin/instructor-applications/{id}` — Detail
- `POST /api/admin/instructor-applications/{id}/approve` — Approve (upgrades role to INSTRUCTOR)
- `POST /api/admin/instructor-applications/{id}/reject` — Reject with optional notes

## Admin Course Management (`AdminCourseController`)
- `PUT /api/admin/courses/{id}` — Edit any course
- `POST /api/admin/courses/{id}/approve` — Approve → PUBLISHED
- `POST /api/admin/courses/{id}/reject` — Reject → CHANGES_REQUESTED with feedback

## Seed Data (`SeedController`)
- `POST /api/public/seed` — Seed 5 demo courses (idempotent)
- `POST /api/public/seed-extra-questions` — Seed 11 non-MCQ questions

---

## Key Field Mappings (Backend DTO ↔ Frontend)
| Backend DTO Field | Frontend Field | Notes |
|---|---|---|
| `LearningUnitResponse.contentType` | `learningUnit.contentType` | Entity field is `type` |
| `AITutorRequest.query` | `request.query` | NOT `message` |
| `AITutorResponse.message` | `response.message` | NOT `response` |
| `Course.estimatedDurationMinutes` | `course.estimatedDurationMinutes` | NOT estimatedHours |
| `Course.createdByName` | `course.createdByName` | NOT instructorName |
| `Course.industryVertical` | `course.industryVertical` | Used as category |
| `AnswerResultResponse.score` | `result.score` | 0.0-1.0 for AI-evaluated |
| `AnswerResultResponse.feedback` | `result.feedback` | AI feedback string |

## See also
- [conventions.md](conventions.md) — Coding standards and naming rules
- [progress.md](progress.md) — Development timeline