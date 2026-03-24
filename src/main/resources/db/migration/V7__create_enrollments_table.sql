-- Tabla de inscripciones - esquema inicial (si no existe)
CREATE TABLE IF NOT EXISTS enrollments (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    course_id BIGINT NOT NULL,
    purchased_hours DECIMAL(5,2) NOT NULL,
    used_hours DECIMAL(5,2) NOT NULL DEFAULT 0.00,
    total_paid DECIMAL(10,2) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'HOURS_EXHAUSTED', 'SUSPENDED', 'CANCELLED', 'EXPIRED')),
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_enrollments_v7_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE RESTRICT,
    CONSTRAINT fk_enrollments_v7_course FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE RESTRICT
);

CREATE INDEX IF NOT EXISTS idx_enrollments_user ON enrollments(user_id);
CREATE INDEX IF NOT EXISTS idx_enrollments_course ON enrollments(course_id);
CREATE INDEX IF NOT EXISTS idx_enrollments_status ON enrollments(status);
CREATE INDEX IF NOT EXISTS idx_enrollments_user_course ON enrollments(user_id, course_id);
CREATE INDEX IF NOT EXISTS idx_enrollments_active_hours ON enrollments(status, purchased_hours, used_hours);