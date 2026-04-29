# API Contracts

REST API reference for the AI Learning Platform backend. This is the source of truth for endpoint paths, request/response shapes, and error handling.

## Base URL
- **Production**: `https://ai-learning-platform-be-production.up.railway.app`
- **Local dev**: `http://localhost:8080`
- All endpoints prefixed with `/api/`

## Authentication
- `Authorization: Bearer <token>` header for all non-public endpoints
- Self-issued HMAC-SHA256 JWT (Nimbus JOSE), 24hr expiry
- JWT subject = user UUID; claims: `roles`, `username`, `email`, `displayName`

## Security Tiers
| Tier | Pattern | Rule |
|---|---|---|
| Public | `/api/public/**`, `GET /api/courses/**` | No auth required |
| Authenticated | All other `/api/**` | Valid JWT required |
| Admin | `/api/admin/**` | `hasRole("ADMIN")` |
| Instructor | Course create/update/publish | `hasAnyRole("INSTRUCTOR", "ADMIN")` |

## Error Responses
| Code | Meaning |
|---|---|
| 400 | Validation error — body has field-level details |
| 401 | Missing or expired JWT |
| 403 | Insufficient role |
| 404 | Resource not found |
| 409 | Conflict (duplicate email/username) |
| 500 | Unexpected backend failure |

---

## Auth (`/api/public/auth`)

| Method | Path | Description |
|---|---|---|
| POST | `/api/public/auth/register` | Create STUDENT, returns JWT |
| POST | `/api/public/auth/login` | Login (username OR email), returns JWT |
| POST | `/api/public/auth/register-instructor` | Create PENDING_INSTRUCTOR, returns JWT |

**Request** (register/login):
```json
{ "username": "...", "email": "...", "password": "...", "displayName": "..." }
```
**Response** (all auth):
```json
{ "token": "...", "userId": "...", "username": "...", "email": "...", "displayName": "...", "avatarUrl": "...", "roles": ["STUDENT"] }
```

---

## Courses (`/api/courses`)

| Method | Path | Auth | Description |
|---|---|---|---|
| GET | `/api/courses` | Public | List published courses |
| GET | `/api/courses/search?q=` | Public | Text search |
| GET | `/api/courses/filter` | Public | Filter by category/difficulty/duration |
| GET | `/api/courses/categories` | Public | Distinct category list |
| GET | `/api/courses/{id}` | Public | Course detail |
| GET | `/api/courses/{id}/tree` | Public | Full tree: modules→topics→concepts→learningUnits |
| GET | `/api/courses/{id}/progress` | Auth | Course progress for current user |
| POST | `/api/courses` | Instructor | Create course |
| PUT | `/api/courses/{id}` | Instructor | Update course |
| POST | `/api/courses/{id}/publish` | Instructor | Submit for approval (DRAFT → PENDING_APPROVAL) |

---

## AI Tutor (`/api/tutor`)

| Method | Path | Auth | Description |
|---|---|---|---|
| POST | `/api/tutor/chat` | Auth | Socratic AI response |

**Request**:
```json
{ "courseId": "...", "conceptId": "...", "query": "...", "sessionId": "..." }
```
- Field is `query` — **NEVER `message`**
- `sessionId` is auto-generated server-side if omitted

**Response**:
```json
{ "message": "...", "sessionId": "...", "hintLevel": 1, "responseType": "...", "suggestedAction": "..." }
```
- Field is `message` — **NEVER `response`**

---

## Assessment (`/api/assessment`)

| Method | Path | Auth | Description |
|---|---|---|---|
| GET | `/api/assessment/concepts/{conceptId}/questions` | Auth | Questions for concept (user-scoped) |
| POST | `/api/assessment/submit` | Auth | Submit an answer |
| GET | `/api/assessment/modules/{moduleId}/diagnostic` | Auth | Diagnostic test for module |
| GET | `/api/assessment/review-queue` | Auth | Concepts due for spaced-repetition review |
| POST | `/api/assessment/concepts/{conceptId}/generate` | Auth | Generate 3 AI questions (GPT-4o, user-scoped) |

**Submit request**:
```json
{ "questionId": "...", "answer": { "answer": "selected option text" }, "timeTakenSeconds": 30, "hintsUsed": 0 }
```
**Submit response**:
```json
{
  "attemptId": "...", "correct": true, "score": 0.9, "explanation": "...",
  "feedback": "AI feedback string", "updatedMastery": 0.72, "nextAction": "advance",
  "xpEarned": 10, "nextConceptId": "..."
}
```
- `score`: 0.0–1.0 (AI-evaluated for SUBJECTIVE/CODING; 0 or 1 for MCQ)
- `nextAction`: `advance` | `reinforce` | `remediate`
- AI questions are scoped to the requesting user — other users do not see them

---

## Enrollment (`/api/enrollments`)

| Method | Path | Auth | Description |
|---|---|---|---|
| POST | `/api/enrollments/{courseId}` | Auth | Enroll (awards 10 XP) |
| DELETE | `/api/enrollments/{courseId}` | Auth | Unenroll |
| GET | `/api/enrollments` | Auth | List enrolled courses |
| GET | `/api/enrollments/{courseId}/status` | Auth | Enrollment status |

---

## Dashboard (`/api/dashboard`)

| Method | Path | Auth | Description |
|---|---|---|---|
| GET | `/api/dashboard` | Auth | Full dashboard: XP, enrolled courses, weak areas, review queue, badges, rank |

**Response shape**:
```json
{
  "userId": "...", "fullName": "...", "totalXp": 150, "currentStreak": 3, "longestStreak": 5,
  "rank": 2, "enrolledCourses": [...], "weakAreas": [...], "recentBadges": [...]
}
```
- `weakAreas`: concepts with `attempts > 0` AND `masteryLevel < 0.6`
- Review queue returned separately from `/api/assessment/review-queue`

---

## Gamification (`/api/gamification`)

| Method | Path | Auth | Description |
|---|---|---|---|
| GET | `/api/gamification/badges` | Auth | User's earned badges |
| GET | `/api/gamification/leaderboard` | Auth | Global XP leaderboard |
| GET | `/api/gamification/xp` | Auth | XP event history |

---

## Learning Path (`/api/learning-path`)

| Method | Path | Auth | Description |
|---|---|---|---|
| GET | `/api/learning-path/courses/{courseId}` | Auth | Full learning path with steps |
| GET | `/api/learning-path/courses/{courseId}/next` | Auth | Next recommended concept |

---

## Certificates (`/api/certificates` + `/api/public/certificates`)

| Method | Path | Auth | Description |
|---|---|---|---|
| POST | `/api/certificates/courses/{courseId}` | Auth | Generate certificate (requires 100% progress) |
| GET | `/api/certificates/my` | Auth | List user's certificates |
| GET | `/api/public/certificates/{verificationCode}` | Public | Verify certificate |
| GET | `/api/public/certificates/{verificationCode}/download` | Public | Download PDF |

---

## User Profile (`/api/users/me`)

| Method | Path | Auth | Description |
|---|---|---|---|
| GET | `/api/users/me` | Auth | Get profile |
| PUT | `/api/users/me` | Auth | Update profile |
| PUT | `/api/users/me/password` | Auth | Change password |
| DELETE | `/api/users/me` | Auth | Delete account |

---

## Learning History (`/api/learning-history`)

| Method | Path | Auth | Description |
|---|---|---|---|
| GET | `/api/learning-history` | Auth | Per-course progress + recent activity |

---

## Instructor (`/api/instructor`)

| Method | Path | Auth | Description |
|---|---|---|---|
| GET | `/api/instructor/courses` | Instructor | List own courses |
| POST | `/api/instructor/courses` | Instructor | Create course |
| PUT | `/api/instructor/courses/{id}` | Instructor | Update course |
| POST | `/api/instructor/courses/{id}/submit-for-approval` | Instructor | Submit for admin review |
| DELETE | `/api/instructor/courses/{id}` | Instructor | Delete course |
| POST | `/api/instructor/courses/import` | Instructor | Import course from DOCX |
| POST | `/api/instructor/courses/{id}/import-content` | Instructor | Import content into existing course |
| POST | `/api/instructor/upload` | Instructor | Upload media (Cloudinary) |
| POST/PUT/DELETE | `/api/instructor/modules/**` | Instructor | Module CRUD |
| POST/PUT/DELETE | `/api/instructor/topics/**` | Instructor | Topic CRUD |
| POST/PUT/DELETE | `/api/instructor/concepts/**` | Instructor | Concept CRUD |

---

## Instructor Application (`/api/instructor-application`)

| Method | Path | Auth | Description |
|---|---|---|---|
| GET | `/api/instructor-application` | Auth | Get own application |
| POST | `/api/instructor-application` | Auth | Submit/update application |

---

## Admin — Instructor Review (`/api/admin/instructor-applications`)

| Method | Path | Auth | Description |
|---|---|---|---|
| GET | `/api/admin/instructor-applications?status=PENDING` | Admin | List by status |
| GET | `/api/admin/instructor-applications/{id}` | Admin | Detail |
| POST | `/api/admin/instructor-applications/{id}/approve` | Admin | Approve → upgrades role to INSTRUCTOR |
| POST | `/api/admin/instructor-applications/{id}/reject` | Admin | Reject with optional notes |

---

## Admin — Course Management (`/api/admin/courses`)

| Method | Path | Auth | Description |
|---|---|---|---|
| GET | `/api/admin/courses/pending` | Admin | List PENDING_APPROVAL courses |
| GET | `/api/admin/courses` | Admin | List all courses |
| POST | `/api/admin/courses/{id}/approve` | Admin | Approve → PUBLISHED |
| POST | `/api/admin/courses/{id}/reject` | Admin | Reject → CHANGES_REQUESTED with feedback |
| POST | `/api/admin/courses/{id}/unpublish` | Admin | Unpublish |
| DELETE | `/api/admin/courses/{id}` | Admin | Delete any course |

---

## Seed Data (`/api/public`)

| Method | Path | Description |
|---|---|---|
| POST | `/api/public/seed` | Seed 5 demo courses (idempotent) |
| POST | `/api/public/seed-extra-questions` | Seed non-MCQ question examples |

---

## Key Field Mappings
| Backend DTO | Frontend field | Pitfall |
|---|---|---|
| `AITutorRequest.query` | `request.query` | NEVER `message` |
| `AITutorResponse.message` | `response.message` | NEVER `response` |
| `LearningUnitResponse.contentType` | `learningUnit.contentType` | entity field is `type` |
| `Course.estimatedDurationMinutes` | `estimatedDurationMinutes` | NEVER `estimatedHours` |
| `Course.createdByName` | `createdByName` | NOT `instructorName` |
| `AnswerResultResponse.score` | `result.score` | 0.0–1.0 float |
| `AnswerResultResponse.feedback` | `result.feedback` | AI feedback string |
| `UserProgressResponse.courseId` | `ReviewItem.courseId` | required for review navigation |

## See also
- [conventions.md](conventions.md) — Coding standards and naming rules
- [progress.md](progress.md) — Development timeline
