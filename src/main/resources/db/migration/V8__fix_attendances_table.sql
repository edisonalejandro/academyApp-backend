-- ===========================================
-- V8: Fix attendances table to match entity
-- ===========================================
-- Add columns missing from V7 that the entity requires
ALTER TABLE attendances ADD COLUMN IF NOT EXISTS attended        BOOLEAN   NOT NULL DEFAULT false;
ALTER TABLE attendances ADD COLUMN IF NOT EXISTS attendance_date TIMESTAMP;
ALTER TABLE attendances ADD COLUMN IF NOT EXISTS arrival_time    TIMESTAMP;
ALTER TABLE attendances ADD COLUMN IF NOT EXISTS departure_time  TIMESTAMP;
ALTER TABLE attendances ADD COLUMN IF NOT EXISTS is_late         BOOLEAN   NOT NULL DEFAULT false;
ALTER TABLE attendances ADD COLUMN IF NOT EXISTS is_excused      BOOLEAN   NOT NULL DEFAULT false;
ALTER TABLE attendances ADD COLUMN IF NOT EXISTS recorded_by     VARCHAR(100);
ALTER TABLE attendances ADD COLUMN IF NOT EXISTS updated_by      VARCHAR(100);

-- Make enrollment_id nullable: the entity does not map this column
-- so inserts done by JPA would fail the NOT NULL constraint
ALTER TABLE attendances ALTER COLUMN enrollment_id DROP NOT NULL;
