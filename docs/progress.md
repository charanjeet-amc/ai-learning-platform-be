# Backend Progress Log

## 2026-04-25
- Fixed `EnrollmentService.updateProgress` transaction isolation bug:
  - Changed to `Propagation.REQUIRES_NEW` so it runs in an independent transaction
  - Previously: `ResourceNotFoundException` inside poisoned the outer `submitAnswer` transaction → silent rollback → submit button did nothing
- AI question user-scoping:
  - V5 migration: added `generated_for_user_id` to `questions`
  - `getQuestionsForConcept` now uses `findByConceptIdForUser` (shows shared + user-specific questions)
  - `generateAIQuestions` stamps `generatedForUserId = userId` before saving
  - V6 migration: deleted orphaned AI questions (created before V5, had NULL user scope, were visible to all)

## 2026-04-24
- Spaced repetition review queue now working:
  - `findDueForReview` JPQL uses `JOIN FETCH` chain to avoid `LazyInitializationException` (`open-in-view: false`)
  - `getReviewQueue` marked `@Transactional(readOnly = true)`
  - `UserProgressResponse` now includes `courseId` field for navigation links
- Weak areas bug fixed: `findWeakConcepts` now filters `attempts > 0` (was showing unattempted concepts)
- Dashboard `completedAt` date bug fixed on FE side: `cert.completedAt ?? cert.issuedAt`

## 2026-04-17
- AI-powered evaluation for SUBJECTIVE/CODING answers (GPT-4o, score 0–1 + detailed feedback)
- Keyword-based fallback when OpenAI API unavailable
- SCENARIO_BASED questions: case-insensitive exact match
- Seed endpoint for non-MCQ questions (`POST /api/public/seed-extra-questions`)
- Fixed: AI tutor `query` field (was `message`), assessment `score`/`feedback` fields in response

## 2026-04-16
- Course approval workflow: DRAFT → PENDING_APPROVAL → PUBLISHED / CHANGES_REQUESTED
- `AdminCourseController` + `AdminCourseService`: approve/reject with feedback
- `CourseStatus` enum expanded

## 2026-04-15
- Instructor onboarding: PENDING_INSTRUCTOR → application → admin approval → INSTRUCTOR
- `InstructorApplicationController` + `AdminInstructorController`
- JWT roles → Spring Security authorities fix (`JwtAuthenticationConverter`)
- Category backfill for existing seed courses

## 2026-04-14
- Gamification: XP events, streaks, badges, leaderboard
- Dashboard, learning history, Redis caching
- `JacksonConfig`: serialize `LocalDateTime` with 'Z' UTC suffix

## 2026-04-13
- Initial setup: Spring Boot 3.4.4, PostgreSQL, Flyway, JWT auth
- Full knowledge graph: Course → Module → Topic → Concept → LearningUnit
- AI Tutor: Socratic method, 4-level hint escalation, GPT-4o
- MCQ assessment with mastery tracking (MasteryCalculator, AdaptiveEngine, SpacedRepetitionEngine)
- Seed data: 5 demo courses

## See also
- [api-contracts.md](api-contracts.md) — Endpoint reference
- [conventions.md](conventions.md) — Coding standards
