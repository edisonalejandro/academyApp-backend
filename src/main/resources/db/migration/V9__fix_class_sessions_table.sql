-- V9: Fix class_sessions table - add missing columns not in V7
ALTER TABLE class_sessions ADD COLUMN IF NOT EXISTS meeting_id           VARCHAR(100);
ALTER TABLE class_sessions ADD COLUMN IF NOT EXISTS meeting_password      VARCHAR(50);
ALTER TABLE class_sessions ADD COLUMN IF NOT EXISTS is_recurring          BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE class_sessions ADD COLUMN IF NOT EXISTS parent_class_id       BIGINT;
ALTER TABLE class_sessions ADD COLUMN IF NOT EXISTS difficulty_level      VARCHAR(50);
ALTER TABLE class_sessions ADD COLUMN IF NOT EXISTS special_requirements  TEXT;
ALTER TABLE class_sessions ADD COLUMN IF NOT EXISTS cancellation_reason   VARCHAR(500);
ALTER TABLE class_sessions ADD COLUMN IF NOT EXISTS created_by            VARCHAR(100);
ALTER TABLE class_sessions ADD COLUMN IF NOT EXISTS updated_by            VARCHAR(100);
