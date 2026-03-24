-- Tabla de cursos (si no existe)
CREATE TABLE IF NOT EXISTS courses (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    code VARCHAR(20) UNIQUE NOT NULL,
    description TEXT,
    dance_type VARCHAR(50) NOT NULL CHECK (dance_type IN ('SALSA', 'BACHATA', 'MERENGUE', 'REGGAETON', 'CUMBIA', 'TANGO', 'KIZOMBA', 'ZOUK', 'MAMBO', 'CHA_CHA_CHA')),
    level VARCHAR(50) NOT NULL CHECK (level IN ('BEGINNER', 'INTERMEDIATE', 'ADVANCED', 'MASTER', 'OPEN')),
    price_per_hour DECIMAL(10,2),
    duration_hours DECIMAL(3,1) DEFAULT 1.5,
    max_capacity INTEGER DEFAULT 20,
    teacher_id BIGINT NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    image_url VARCHAR(500),
    prerequisites TEXT,
    objectives TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_courses_teacher FOREIGN KEY (teacher_id) REFERENCES users(id) ON DELETE RESTRICT
);

CREATE INDEX IF NOT EXISTS idx_courses_dance_type ON courses(dance_type);
CREATE INDEX IF NOT EXISTS idx_courses_level ON courses(level);
CREATE INDEX IF NOT EXISTS idx_courses_teacher ON courses(teacher_id);
CREATE INDEX IF NOT EXISTS idx_courses_active ON courses(is_active);
CREATE INDEX IF NOT EXISTS idx_courses_code ON courses(code);

-- Insertar cursos de ejemplo
INSERT INTO courses (title, code, description, dance_type, level, price_per_hour, teacher_id) VALUES
('Salsa Básica para Principiantes', 'SAL-B-001', 'Aprende los fundamentos de la salsa desde cero', 'SALSA', 'BEGINNER', 15000.00, 2),
('Bachata Intermedia', 'BAC-I-001', 'Perfecciona tu bachata con pasos más avanzados', 'BACHATA', 'INTERMEDIATE', 18000.00, 3),
('Reggaeton Avanzado', 'REG-A-001', 'Domina los movimientos más complejos del reggaeton', 'REGGAETON', 'ADVANCED', 20000.00, 4),
('Merengue para Todos', 'MER-O-001', 'Merengue tradicional y moderno para todos los niveles', 'MERENGUE', 'OPEN', 16000.00, 2),
('Kizomba Principiantes', 'KIZ-B-001', 'Introducción al sensual baile de kizomba', 'KIZOMBA', 'BEGINNER', 17000.00, 3);