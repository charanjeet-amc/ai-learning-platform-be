CREATE TABLE certificates (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    course_id UUID NOT NULL REFERENCES courses(id) ON DELETE CASCADE,
    verification_code VARCHAR(36) NOT NULL UNIQUE,
    issued_at TIMESTAMP NOT NULL DEFAULT NOW(),
    completed_at TIMESTAMP,
    progress_percent DOUBLE PRECISION DEFAULT 100.0,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    CONSTRAINT uq_certificate_user_course UNIQUE (user_id, course_id)
);

CREATE INDEX idx_certificates_verification_code ON certificates(verification_code);
CREATE INDEX idx_certificates_user_id ON certificates(user_id);
