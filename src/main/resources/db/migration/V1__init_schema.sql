-- V1__init_schema.sql
-- AI Learning Platform - Initial Schema

-- ==================== ENUMS ====================
CREATE TYPE difficulty_level AS ENUM ('BEGINNER', 'INTERMEDIATE', 'ADVANCED', 'EXPERT');
CREATE TYPE content_type AS ENUM ('TEXT', 'VIDEO', 'INTERACTIVE', 'CODE_EXERCISE', 'DIAGRAM', 'QUIZ', 'SIMULATION', 'AUDIO');
CREATE TYPE concept_status AS ENUM ('NOT_STARTED', 'IN_PROGRESS', 'STRUGGLING', 'MASTERED', 'REVIEW_NEEDED');
CREATE TYPE question_type AS ENUM ('MULTIPLE_CHOICE', 'TRUE_FALSE', 'SHORT_ANSWER', 'CODE_COMPLETION', 'CODE_DEBUG', 'MATCHING', 'ORDER');
CREATE TYPE learning_style AS ENUM ('VISUAL', 'READING', 'KINESTHETIC', 'AUDITORY');
CREATE TYPE learning_pace AS ENUM ('SLOW', 'MODERATE', 'FAST', 'SELF_PACED');
CREATE TYPE subscription_tier AS ENUM ('FREE', 'BASIC', 'PRO', 'ENTERPRISE');
CREATE TYPE payment_status AS ENUM ('PENDING', 'COMPLETED', 'FAILED', 'REFUNDED');
CREATE TYPE user_role AS ENUM ('STUDENT', 'INSTRUCTOR', 'ADMIN', 'CONTENT_CREATOR');

-- ==================== USERS ====================
CREATE TABLE users (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    keycloak_id     VARCHAR(255) NOT NULL UNIQUE,
    email           VARCHAR(255) NOT NULL UNIQUE,
    username        VARCHAR(100) NOT NULL UNIQUE,
    display_name    VARCHAR(200),
    avatar_url      VARCHAR(500),
    role            user_role NOT NULL DEFAULT 'STUDENT',
    subscription_tier subscription_tier NOT NULL DEFAULT 'FREE',
    total_xp        BIGINT NOT NULL DEFAULT 0,
    current_streak  INT NOT NULL DEFAULT 0,
    longest_streak  INT NOT NULL DEFAULT 0,
    last_active_date DATE,
    bio             TEXT,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_users_keycloak ON users(keycloak_id);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_xp ON users(total_xp DESC);

-- ==================== COURSES ====================
CREATE TABLE courses (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title           VARCHAR(300) NOT NULL,
    slug            VARCHAR(300) NOT NULL UNIQUE,
    description     TEXT,
    short_description VARCHAR(500),
    thumbnail_url   VARCHAR(500),
    difficulty      difficulty_level NOT NULL DEFAULT 'BEGINNER',
    category        VARCHAR(100),
    tags            TEXT[],
    instructor_id   UUID REFERENCES users(id),
    published       BOOLEAN NOT NULL DEFAULT FALSE,
    rating          DOUBLE PRECISION DEFAULT 0.0,
    enrollment_count INT NOT NULL DEFAULT 0,
    estimated_hours INT,
    prerequisites   TEXT,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_courses_slug ON courses(slug);
CREATE INDEX idx_courses_category ON courses(category);
CREATE INDEX idx_courses_published ON courses(published);

-- ==================== MODULES ====================
CREATE TABLE modules (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    course_id       UUID NOT NULL REFERENCES courses(id) ON DELETE CASCADE,
    title           VARCHAR(300) NOT NULL,
    description     TEXT,
    order_index     INT NOT NULL,
    learning_objectives TEXT[],
    estimated_minutes INT,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE(course_id, order_index)
);

CREATE INDEX idx_modules_course ON modules(course_id);

-- ==================== TOPICS ====================
CREATE TABLE topics (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    module_id       UUID NOT NULL REFERENCES modules(id) ON DELETE CASCADE,
    title           VARCHAR(300) NOT NULL,
    description     TEXT,
    order_index     INT NOT NULL,
    tags            TEXT[],
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE(module_id, order_index)
);

CREATE INDEX idx_topics_module ON topics(module_id);

-- ==================== CONCEPTS (Knowledge Graph Nodes) ====================
CREATE TABLE concepts (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    topic_id        UUID NOT NULL REFERENCES topics(id) ON DELETE CASCADE,
    title           VARCHAR(300) NOT NULL,
    description     TEXT,
    difficulty      difficulty_level NOT NULL DEFAULT 'BEGINNER',
    order_index     INT NOT NULL,
    mastery_threshold DOUBLE PRECISION NOT NULL DEFAULT 0.8,
    estimated_minutes INT DEFAULT 15,
    status          concept_status DEFAULT 'NOT_STARTED',
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_concepts_topic ON concepts(topic_id);

-- ==================== CONCEPT DEPENDENCIES (Knowledge Graph Edges) ====================
CREATE TABLE concept_dependencies (
    concept_id      UUID NOT NULL REFERENCES concepts(id) ON DELETE CASCADE,
    dependency_id   UUID NOT NULL REFERENCES concepts(id) ON DELETE CASCADE,
    PRIMARY KEY (concept_id, dependency_id),
    CHECK (concept_id != dependency_id)
);

-- ==================== CONCEPT MISCONCEPTIONS ====================
CREATE TABLE concept_misconceptions (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    concept_id      UUID NOT NULL REFERENCES concepts(id) ON DELETE CASCADE,
    misconception   TEXT NOT NULL,
    correction      TEXT NOT NULL,
    frequency       INT NOT NULL DEFAULT 0,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_misconceptions_concept ON concept_misconceptions(concept_id);

-- ==================== CONCEPT SOCRATIC QUESTIONS ====================
CREATE TABLE concept_socratic_questions (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    concept_id      UUID NOT NULL REFERENCES concepts(id) ON DELETE CASCADE,
    question        TEXT NOT NULL,
    purpose         VARCHAR(200),
    hint_level      INT NOT NULL DEFAULT 1,
    order_index     INT NOT NULL DEFAULT 0,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_socratic_concept ON concept_socratic_questions(concept_id);

-- ==================== CONCEPT OUTCOMES ====================
CREATE TABLE concept_outcomes (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    concept_id      UUID NOT NULL REFERENCES concepts(id) ON DELETE CASCADE,
    outcome         TEXT NOT NULL,
    measurable      BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_outcomes_concept ON concept_outcomes(concept_id);

-- ==================== LEARNING UNITS ====================
CREATE TABLE learning_units (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    concept_id      UUID NOT NULL REFERENCES concepts(id) ON DELETE CASCADE,
    title           VARCHAR(300) NOT NULL,
    content_type    content_type NOT NULL,
    content         JSONB NOT NULL DEFAULT '{}',
    order_index     INT NOT NULL,
    duration_minutes INT,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_units_concept ON learning_units(concept_id);

-- ==================== USER CONCEPT PROGRESS ====================
CREATE TABLE user_concept_progress (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    concept_id      UUID NOT NULL REFERENCES concepts(id) ON DELETE CASCADE,
    mastery_score   DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    confidence      DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    attempts        INT NOT NULL DEFAULT 0,
    correct_attempts INT NOT NULL DEFAULT 0,
    hints_used      INT NOT NULL DEFAULT 0,
    time_spent_minutes INT NOT NULL DEFAULT 0,
    status          concept_status NOT NULL DEFAULT 'NOT_STARTED',
    last_reviewed   TIMESTAMP,
    next_review     TIMESTAMP,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE(user_id, concept_id)
);

CREATE INDEX idx_progress_user ON user_concept_progress(user_id);
CREATE INDEX idx_progress_concept ON user_concept_progress(concept_id);
CREATE INDEX idx_progress_mastery ON user_concept_progress(mastery_score);

-- ==================== USER LEARNING PROFILE ====================
CREATE TABLE user_learning_profiles (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    preferred_style learning_style DEFAULT 'VISUAL',
    pace            learning_pace DEFAULT 'MODERATE',
    visual_score    DOUBLE PRECISION DEFAULT 0.5,
    reading_score   DOUBLE PRECISION DEFAULT 0.5,
    kinesthetic_score DOUBLE PRECISION DEFAULT 0.5,
    auditory_score  DOUBLE PRECISION DEFAULT 0.5,
    difficulty_preference difficulty_level DEFAULT 'BEGINNER',
    daily_goal_minutes INT DEFAULT 30,
    session_duration_avg INT DEFAULT 0,
    total_learning_hours INT DEFAULT 0,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

-- ==================== USER WEAK AREAS ====================
CREATE TABLE user_weak_areas (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    concept_id      UUID NOT NULL REFERENCES concepts(id) ON DELETE CASCADE,
    weakness_score  DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    identified_at   TIMESTAMP NOT NULL DEFAULT NOW(),
    resolved        BOOLEAN NOT NULL DEFAULT FALSE,
    resolved_at     TIMESTAMP,
    UNIQUE(user_id, concept_id)
);

CREATE INDEX idx_weak_areas_user ON user_weak_areas(user_id);

-- ==================== AI INTERACTIONS ====================
CREATE TABLE ai_interactions (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    concept_id      UUID REFERENCES concepts(id),
    session_id      VARCHAR(100) NOT NULL,
    user_message    TEXT NOT NULL,
    ai_response     TEXT NOT NULL,
    hint_level      INT DEFAULT 0,
    interaction_type VARCHAR(50) DEFAULT 'CHAT',
    tokens_used     INT DEFAULT 0,
    model_used      VARCHAR(50),
    created_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_ai_user ON ai_interactions(user_id);
CREATE INDEX idx_ai_session ON ai_interactions(session_id);
CREATE INDEX idx_ai_concept ON ai_interactions(concept_id);

-- ==================== QUESTIONS ====================
CREATE TABLE questions (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    concept_id      UUID NOT NULL REFERENCES concepts(id) ON DELETE CASCADE,
    question_type   question_type NOT NULL,
    question_text   TEXT NOT NULL,
    correct_answer  TEXT NOT NULL,
    explanation     TEXT,
    difficulty      difficulty_level NOT NULL DEFAULT 'BEGINNER',
    metadata        JSONB DEFAULT '{}',
    order_index     INT NOT NULL DEFAULT 0,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_questions_concept ON questions(concept_id);
CREATE INDEX idx_questions_type ON questions(question_type);
CREATE INDEX idx_questions_difficulty ON questions(difficulty);

-- ==================== USER ATTEMPTS ====================
CREATE TABLE user_attempts (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    question_id     UUID NOT NULL REFERENCES questions(id) ON DELETE CASCADE,
    answer          JSONB NOT NULL,
    correct         BOOLEAN NOT NULL,
    time_taken_seconds INT,
    attempt_number  INT NOT NULL DEFAULT 1,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_attempts_user ON user_attempts(user_id);
CREATE INDEX idx_attempts_question ON user_attempts(question_id);

-- ==================== LEARNING PATHS ====================
CREATE TABLE learning_paths (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    course_id       UUID NOT NULL REFERENCES courses(id) ON DELETE CASCADE,
    title           VARCHAR(300),
    description     TEXT,
    active          BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_paths_user ON learning_paths(user_id);

-- ==================== LEARNING STEPS ====================
CREATE TABLE learning_steps (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    path_id         UUID NOT NULL REFERENCES learning_paths(id) ON DELETE CASCADE,
    concept_id      UUID NOT NULL REFERENCES concepts(id),
    order_index     INT NOT NULL,
    completed       BOOLEAN NOT NULL DEFAULT FALSE,
    completed_at    TIMESTAMP,
    skipped         BOOLEAN NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_steps_path ON learning_steps(path_id);

-- ==================== ENROLLMENTS ====================
CREATE TABLE enrollments (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    course_id       UUID NOT NULL REFERENCES courses(id) ON DELETE CASCADE,
    enrolled_at     TIMESTAMP NOT NULL DEFAULT NOW(),
    completed_at    TIMESTAMP,
    progress        DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    current_concept_id UUID REFERENCES concepts(id),
    active          BOOLEAN NOT NULL DEFAULT TRUE,
    UNIQUE(user_id, course_id)
);

CREATE INDEX idx_enrollments_user ON enrollments(user_id);
CREATE INDEX idx_enrollments_course ON enrollments(course_id);

-- ==================== BADGES ====================
CREATE TABLE badges (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name            VARCHAR(100) NOT NULL UNIQUE,
    description     VARCHAR(500),
    icon            VARCHAR(100),
    criteria        VARCHAR(500),
    xp_reward       INT NOT NULL DEFAULT 0,
    category        VARCHAR(50),
    created_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

-- ==================== USER BADGES ====================
CREATE TABLE user_badges (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    badge_id        UUID NOT NULL REFERENCES badges(id) ON DELETE CASCADE,
    earned_at       TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE(user_id, badge_id)
);

CREATE INDEX idx_user_badges ON user_badges(user_id);

-- ==================== XP EVENTS ====================
CREATE TABLE xp_events (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    amount          INT NOT NULL,
    reason          VARCHAR(200) NOT NULL,
    source_type     VARCHAR(50),
    source_id       UUID,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_xp_user ON xp_events(user_id);
CREATE INDEX idx_xp_created ON xp_events(created_at);

-- ==================== PAYMENTS ====================
CREATE TABLE payments (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id             UUID NOT NULL REFERENCES users(id),
    stripe_payment_id   VARCHAR(255) UNIQUE,
    stripe_customer_id  VARCHAR(255),
    amount              DECIMAL(10, 2) NOT NULL,
    currency            VARCHAR(3) NOT NULL DEFAULT 'USD',
    status              payment_status NOT NULL DEFAULT 'PENDING',
    description         VARCHAR(500),
    subscription_tier   subscription_tier,
    period_start        TIMESTAMP,
    period_end          TIMESTAMP,
    created_at          TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_payments_user ON payments(user_id);
CREATE INDEX idx_payments_stripe ON payments(stripe_payment_id);

-- ==================== NOTIFICATIONS ====================
CREATE TABLE notifications (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title           VARCHAR(200) NOT NULL,
    message         TEXT NOT NULL,
    type            VARCHAR(50) NOT NULL DEFAULT 'INFO',
    read            BOOLEAN NOT NULL DEFAULT FALSE,
    link            VARCHAR(500),
    created_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_notifications_user ON notifications(user_id);
CREATE INDEX idx_notifications_unread ON notifications(user_id, read) WHERE read = FALSE;

-- ==================== MEDIA ====================
CREATE TABLE media (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    filename        VARCHAR(500) NOT NULL,
    original_name   VARCHAR(500),
    content_type    VARCHAR(100),
    size_bytes      BIGINT,
    url             VARCHAR(1000) NOT NULL,
    uploaded_by     UUID REFERENCES users(id),
    created_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

-- ==================== SEED DATA: BADGES ====================
INSERT INTO badges (name, description, icon, criteria, xp_reward, category) VALUES
('First Steps', 'Complete your first concept', 'rocket', 'Complete 1 concept', 50, 'MILESTONE'),
('Quick Learner', 'Master 5 concepts', 'zap', 'Master 5 concepts', 100, 'MILESTONE'),
('Knowledge Seeker', 'Master 25 concepts', 'book-open', 'Master 25 concepts', 250, 'MILESTONE'),
('Scholar', 'Master 100 concepts', 'graduation-cap', 'Master 100 concepts', 500, 'MILESTONE'),
('Course Completer', 'Complete your first course', 'award', 'Complete 1 course', 200, 'COURSE'),
('Streak Starter', 'Maintain a 3-day streak', 'flame', '3-day streak', 75, 'STREAK'),
('Streak Master', 'Maintain a 7-day streak', 'flame', '7-day streak', 150, 'STREAK'),
('Streak Legend', 'Maintain a 30-day streak', 'flame', '30-day streak', 500, 'STREAK'),
('Perfect Score', 'Get 100% on an assessment', 'star', 'Perfect assessment score', 100, 'ASSESSMENT'),
('AI Explorer', 'Have 10 conversations with AI tutor', 'bot', '10 AI interactions', 75, 'AI'),
('Socratic Thinker', 'Have 50 conversations with AI tutor', 'brain', '50 AI interactions', 200, 'AI'),
('XP Collector', 'Earn 1000 XP', 'gem', 'Accumulate 1000 XP', 100, 'XP'),
('XP Master', 'Earn 10000 XP', 'crown', 'Accumulate 10000 XP', 500, 'XP'),
('Social Learner', 'Enroll in 5 courses', 'users', 'Enroll in 5 courses', 150, 'SOCIAL'),
('Night Owl', 'Complete a session after midnight', 'moon', 'Session after midnight', 50, 'FUN');
