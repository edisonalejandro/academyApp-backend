-- ===========================================
-- V3: Cursos de Baile
-- ===========================================
-- Depende de: V1 (users → teacher_id FK)
-- Los datos de seed son gestionados por DataInitializer en tiempo de ejecución.

CREATE TABLE IF NOT EXISTS courses (
    id           BIGSERIAL      PRIMARY KEY,
    title        VARCHAR(100)   NOT NULL,
    code         VARCHAR(20)    UNIQUE NOT NULL,
    description  TEXT,
    dance_type   VARCHAR(50)    NOT NULL
        CHECK (dance_type IN ('SALSA','BACHATA','MERENGUE','REGGAETON','CUMBIA',
                              'TANGO','KIZOMBA','ZOUK','MAMBO','CHA_CHA_CHA')),
    level        VARCHAR(50)    NOT NULL
        CHECK (level IN ('BEGINNER','INTERMEDIATE','ADVANCED','MASTER','OPEN')),
    price_per_hour  DECIMAL(10,2),
    duration_hours  DECIMAL(3,1)  DEFAULT 1.5,
    max_capacity    INTEGER       DEFAULT 20,
    teacher_id      BIGINT        NOT NULL,
    is_active       BOOLEAN       NOT NULL DEFAULT TRUE,
    image_url       VARCHAR(500),
    prerequisites   TEXT,
    objectives      TEXT,
    created_at      TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_courses_teacher FOREIGN KEY (teacher_id) REFERENCES users(id) ON DELETE RESTRICT
);

CREATE INDEX IF NOT EXISTS idx_courses_dance_type ON courses(dance_type);
CREATE INDEX IF NOT EXISTS idx_courses_level      ON courses(level);
CREATE INDEX IF NOT EXISTS idx_courses_teacher    ON courses(teacher_id);
CREATE INDEX IF NOT EXISTS idx_courses_active     ON courses(is_active);
CREATE INDEX IF NOT EXISTS idx_courses_code       ON courses(code);
