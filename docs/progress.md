# Backend Progress Log

## 2026-04-17
- AI-powered evaluation for SUBJECTIVE and CODING answers using GPT-4o (OpenAI SDK 4.31.0)
- Keyword-based fallback when OpenAI API fails
- Score returned as 0.0-1.0 float, feedback as detailed string
- SCENARIO_BASED questions: case-insensitive exact match
- Seed endpoint for non-MCQ questions (`POST /api/public/seed-extra-questions`)
- Bugs fixed: AI tutor `query` field (was `message`), assessment `score`/`feedback` fields in response

## 2026-04-16
- Course approval workflow: DRAFT → PENDING_APPROVAL → PUBLISHED / CHANGES_REQUESTED
- Admin course review endpoints (approve/reject with feedback)
- `AdminCourseController` + `AdminCourseService`
- CourseStatus enum expanded
- Bugs fixed: publish endpoint permissions, status transition validation

## 2026-04-15
- Instructor onboarding flow (PENDING_INSTRUCTOR role → application → admin approval → INSTRUCTOR)
- `InstructorApplicationController` + `AdminInstructorController`
- Course editor (create/update via `CourseController`)
- Bugs fixed: instructor application duplicate check, role upgrade on approval

## 2026-04-14
- Gamification system: XP awards (10 XP enrollment, 5-25 XP per correct answer), badge system, streaks, leaderboard
- `GamificationController`, `DashboardController`, `LearningHistoryController`
- Redis caching for leaderboard and course lists
- Bugs fixed: XP double-award on re-enrollment, leaderboard cache invalidation

## 2026-04-13
- Initial project setup: Spring Boot 3.4.4, PostgreSQL, JWT auth
- Course catalog with modules → topics → concepts → learningUnits hierarchy
- Course player tree endpoint (`/api/courses/{id}/tree`)
- AI tutor (Socratic method, 4-level hint escalation via GPT-4o)
- MCQ assessment with mastery tracking
- Seed data: 5 demo courses
- Bugs fixed: JWT expiry timezone, CORS config for Vite dev server

## See also
- [conventions.md](conventions.md) — Coding standards
- [api-contracts.md](api-contracts.md) — Endpoint reference