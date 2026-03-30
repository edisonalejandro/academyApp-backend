-- V10: Fix roles table + create missing student_discounts table

-- 1. Add missing description column to roles
ALTER TABLE roles ADD COLUMN IF NOT EXISTS description VARCHAR(255);

-- 2. Create student_discounts table (missing entirely)
CREATE TABLE IF NOT EXISTS student_discounts (
    id                    BIGSERIAL      PRIMARY KEY,
    user_id               BIGINT         NOT NULL,
    category              VARCHAR(30)    NOT NULL
        CHECK (category IN ('REGULAR','UNIVERSITY','COUPLE','SENIOR','CHILD')),
    discount_percentage   DECIMAL(5,2)   NOT NULL,
    verification_document VARCHAR(255),
    is_verified           BOOLEAN        NOT NULL DEFAULT FALSE,
    verified_by           BIGINT,
    valid_from            TIMESTAMP,
    valid_until           TIMESTAMP,
    created_at            TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at            TIMESTAMP      DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_student_discounts_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_student_discounts_user     ON student_discounts(user_id);
CREATE INDEX IF NOT EXISTS idx_student_discounts_category ON student_discounts(category);
CREATE INDEX IF NOT EXISTS idx_student_discounts_active   ON student_discounts(is_verified);
