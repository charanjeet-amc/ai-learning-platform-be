ALTER TABLE questions ADD COLUMN IF NOT EXISTS generated_for_user_id UUID;
CREATE INDEX IF NOT EXISTS idx_questions_generated_for_user ON questions(generated_for_user_id) WHERE generated_for_user_id IS NOT NULL;
