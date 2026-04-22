-- Ensure the completed column exists (Hibernate ddl-auto:update may have added it as nullable)
ALTER TABLE enrollments ADD COLUMN IF NOT EXISTS completed BOOLEAN;

-- Backfill: any enrollment with 100% progress or a completion timestamp is completed
UPDATE enrollments
SET completed = TRUE
WHERE (progress >= 100.0 OR completed_at IS NOT NULL)
  AND (completed IS NULL OR completed = FALSE);

-- All remaining NULL values are not completed
UPDATE enrollments
SET completed = FALSE
WHERE completed IS NULL;

-- Enforce NOT NULL with a sensible default going forward
ALTER TABLE enrollments
  ALTER COLUMN completed SET NOT NULL,
  ALTER COLUMN completed SET DEFAULT FALSE;
