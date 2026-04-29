# Adaptive Learning — Implementation Backlog

Tracks partially-implemented and not-yet-built adaptive learning features.
Work through Phase 1 first (code exists, just needs wiring), then Phase 2, then Phase 3.

---

## Phase 1 — Wire Up Existing Code

Code already exists; needs to be called at the right points in the learning flow.

### 1. Weak Area Score Updates ✅ DONE (2026-04-27)
- `weaknessScore = masteryComponent*0.6 + errorRate*0.3 + hintsComponent*0.1` (capped 0–1)
- `AssessmentServiceImpl.submitAnswer` now calls `updateWeakArea()` after every answer
- On mastery ≥ 0.85: record deleted from `user_weak_areas`
- `DashboardServiceImpl` now reads `UserWeakArea` ordered by `weaknessScore DESC` instead of flat `masteryLevel ASC`
- `UserWeakAreaRepository`: added `findWeakAreasByUserId` (JOIN FETCH, score > 0) and `findByUserIdAndConceptId`

### 2. Frustration Score ✅ DONE (2026-04-27)
- `frustrationScore` field added to `UserConceptProgress` (V7 migration)
- Computed after every answer: `wrong*0.5 + timeTakenSeconds/60*0.3 + hints*0.2` (uses cumulative wrong + hints, current question time)
- Persisted on `progress` before save — both assessment flow and AI tutor now read it
- `AdaptiveEngine.determineNextAction(mastery, frustrationScore)`: if frustration ≥ 2.0 AND not mastered → "remediate" (bypasses "reinforce")
- `AITutorServiceImpl` updated to pass stored `frustrationScore` alongside mastery

### 3. Fast-Track ✅ DONE (2026-04-27)
- `canFastTrack()` called in `AssessmentServiceImpl.submitAnswer`: if `justMastered && attempts ≤ 2 && mastery ≥ 0.90` → `progress.setFastTracked(true)`, response includes `fastTracked: true`
- `LearningPathStepResponse` now includes `fastTracked` field; populated from `progress.getFastTracked()` in `LearningPathServiceImpl`
- FE: `AnswerResult` and `LearningPathStep` types updated with `fastTracked?: boolean`
- FE: `QuizView` shows ⚡ "Fast-tracked!" badge in result feedback when `result.fastTracked === true`
- FE: `CourseTree` shows ⚡ icon on fast-tracked concepts; `CoursePlayerPage` progressMap includes `fastTracked`

### 4. Learning Pace Adaptation ✅ DONE (2026-04-28)
- `SpacedRepetitionEngine.scheduleReview(progress, mastery, pace)` overload applies interval multiplier: SLOW 0.75×, MEDIUM 1.0×, FAST 1.5×
- `AssessmentServiceImpl.submitAnswer` loads `UserLearningProfile` and passes `pace` to `scheduleReview`
- `LearningPathServiceImpl.getPersonalizedPath` loads pace and sets `sessionConceptLimit` (SLOW: 3, MEDIUM: 5, FAST: 8) on response
- FE: `LearningPath` type has `sessionConceptLimit?: number`; CoursePlayerPage "Up next" panel shows "Suggested session: N concepts"

### 5. Learning Style Content Sequencing ✅ DONE (2026-04-28)
- `CourseService.getCourseWithTree(courseId, userId)` overload added; loads user's `preferredStyle` from `UserLearningProfile`
- `CourseController.getCourseWithTree` now extracts userId from JWT and calls user-aware method (null-safe; falls back to `orderIndex` order for unauthenticated)
- `CourseServiceImpl.stylePriority(ContentType, LearningStyle)` priority table: VISUAL→DIAGRAM(0)/VIDEO(1)/INTERACTIVE(2); TEXT→TEXT(0); CODE→CODE(0)/EXERCISE(1); AUDITORY→VIDEO(0)/INTERACTIVE(1); non-preferred types → 99 (maintain orderIndex within tier)
- FE: no changes needed — `ContentViewer` renders units in the order BE returns them

---

## Phase 2 — High-Value Gaps (New Logic Required)

### 6. Session Continuity (Resume Where You Left Off) ✅ DONE (2026-04-29)
- Reuses existing `enrollment.currentConceptId` column (no migration needed)
- `EnrollmentService.trackConceptVisit(userId, courseId, conceptId)` updates `currentConceptId`; wired to `PUT /api/enrollments/{courseId}/last-visited`
- `LearningPathServiceImpl` reads enrollment's `currentConceptId` and exposes it as `lastVisitedConceptId` + `lastVisitedConceptTitle` on `LearningPathResponse` (suppressed when it equals the first concept — no meaningful resume point)
- FE: `LearningPath` type updated; `enrollmentApi` has `useTrackConceptVisitMutation`
- FE: `CoursePlayerPage` session-restore effect waits for `learningPath` to load before navigating — resumes at `lastVisitedConceptId` if set, otherwise falls back to first module
- FE: `handleTreeSelect` + `navigateToNode` fire `trackConceptVisit` on every concept open (fire-and-forget)

### 7. Misconception-Triggered Targeted Remediation ✅ DONE (2026-04-29)
- `ai/MisconceptionDetector` — keyword extraction (≥5 chars, stopwords filtered) from each `ConceptMisconception` text; `anyMatch` against the user's wrong answer; first matching misconception text returned
- `V8__misconception_tracking.sql` — `triggered_misconception TEXT` column on `user_attempts`
- `UserAttempt` entity: `triggeredMisconception` field persisted on every wrong answer where a match is found
- `AssessmentServiceImpl.submitAnswer`: detects before attempt save; `AnswerResultResponse` includes `misconceptionTriggered` + `misconceptionText`
- FE: `AnswerResult` type updated; `QuizView` shows amber callout card "Common Misconception Detected" with the misconception text when `misconceptionTriggered === true`

---

## Phase 3 — Advanced Features (Significant Build)

### 8. Predictive Performance Modeling / Early Warning
- **Status:** Not implemented
- **What's needed:** Analyze trend in mastery scores over time; if trajectory is declining or stagnant, flag the student (email/notification or dashboard warning). Could use simple linear regression on recent `UserAttempt` scores per concept.
- **Complexity:** Medium-high — needs scheduled job (`@Scheduled`) + notification system

### 9. Knowledge Graph Visualization for Learners
- **Status:** Not implemented
- **What's needed:** FE page showing course concepts as nodes (sized/colored by mastery), with dependency edges. Click node → navigate to concept.
- **BE:** Expose concept dependency graph via API (dependencies already in `Concept.dependencyIds`)
- **FE:** New page using a graph library (e.g. React Flow or D3-force); nodes colored by `ConceptStatus`
- **Complexity:** High — primarily FE work

### 10. Collaborative Filtering ("Similar Learners Also Studied…")
- **Status:** Not implemented
- **What's needed:** Find users with similar mastery profiles; recommend concepts/courses they completed that the current user hasn't started. Requires user-similarity computation (cosine similarity on mastery vectors or item-based CF).
- **Complexity:** High — needs a recommendation pipeline, possibly a scheduled batch job

---

## Notes
- When implementing Phase 1 items, read the existing methods carefully before wiring — some may have signature mismatches or incomplete implementations that need patching first.
- Update `docs/progress.md` (BE + FE) when each item ships.
- New Flyway migrations needed for: Phase 2 item 6 (`lastVisitedConceptId` on enrollment).
