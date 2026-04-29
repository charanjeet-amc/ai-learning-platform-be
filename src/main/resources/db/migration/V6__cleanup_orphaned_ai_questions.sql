-- Remove AI-generated questions that have no user scope (created before the user-scoping fix).
-- These were saved globally and are visible to all users, which is incorrect.
-- Deletes related user_attempts first to satisfy FK constraint.
DELETE FROM user_attempts WHERE question_id IN (
    SELECT id FROM questions WHERE ai_generated = true AND generated_for_user_id IS NULL
);
DELETE FROM questions WHERE ai_generated = true AND generated_for_user_id IS NULL;
