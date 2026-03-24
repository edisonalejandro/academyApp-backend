-- Tabla de sesiones de clase
CREATE TABLE IF NOT EXISTS class_sessions (
    id BIGSERIAL PRIMARY KEY,
    course_id BIGINT NOT NULL,
    teacher_id BIGINT NOT NULL,
    session_name VARCHAR(150) NOT NULL,
    description TEXT,
    scheduled_date TIMESTAMP NOT NULL,
    actual_start_time TIMESTAMP,
    actual_end_time TIMESTAMP,
    planned_duration DECIMAL(3,2) NOT NULL DEFAULT 1.50,
    actual_duration DECIMAL(3,2),
    status VARCHAR(30) NOT NULL DEFAULT 'SCHEDULED' CHECK (status IN ('SCHEDULED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED', 'POSTPONED', 'NO_SHOW')),
    max_capacity INTEGER,
    location VARCHAR(255),
    topic VARCHAR(255),
    required_materials TEXT,
    teacher_notes TEXT,
    virtual_meeting_url VARCHAR(500),
    is_virtual BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_class_sessions_course FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE RESTRICT,
    CONSTRAINT fk_class_sessions_teacher FOREIGN KEY (teacher_id) REFERENCES users(id) ON DELETE RESTRICT
);

CREATE INDEX IF NOT EXISTS idx_class_sessions_course ON class_sessions(course_id);
CREATE INDEX IF NOT EXISTS idx_class_sessions_teacher ON class_sessions(teacher_id);
CREATE INDEX IF NOT EXISTS idx_class_sessions_date ON class_sessions(scheduled_date);
CREATE INDEX IF NOT EXISTS idx_class_sessions_status ON class_sessions(status);
CREATE INDEX IF NOT EXISTS idx_class_sessions_course_date ON class_sessions(course_id, scheduled_date);

-- Tabla de asistencias
CREATE TABLE IF NOT EXISTS attendances (
    id BIGSERIAL PRIMARY KEY,
    student_id BIGINT NOT NULL,
    class_session_id BIGINT NOT NULL,
    enrollment_id BIGINT NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'REGISTERED' CHECK (status IN ('REGISTERED', 'PRESENT', 'ABSENT', 'LATE', 'EXCUSED')),
    check_in_time TIMESTAMP,
    check_out_time TIMESTAMP,
    hours_consumed DECIMAL(3,2),
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uq_student_session UNIQUE (student_id, class_session_id),
    CONSTRAINT fk_attendances_student FOREIGN KEY (student_id) REFERENCES users(id) ON DELETE RESTRICT,
    CONSTRAINT fk_attendances_session FOREIGN KEY (class_session_id) REFERENCES class_sessions(id) ON DELETE CASCADE,
    CONSTRAINT fk_attendances_enrollment FOREIGN KEY (enrollment_id) REFERENCES enrollments(id) ON DELETE RESTRICT
);

CREATE INDEX IF NOT EXISTS idx_attendances_student ON attendances(student_id);
CREATE INDEX IF NOT EXISTS idx_attendances_session ON attendances(class_session_id);
CREATE INDEX IF NOT EXISTS idx_attendances_enrollment ON attendances(enrollment_id);
CREATE INDEX IF NOT EXISTS idx_attendances_status ON attendances(status);
CREATE INDEX IF NOT EXISTS idx_attendances_check_in ON attendances(check_in_time);

-- Insertar algunas sesiones de ejemplo
INSERT INTO class_sessions (course_id, teacher_id, session_name, scheduled_date, location, topic) VALUES
(1, 2, 'Salsa Básica - Introducción', '2025-08-10 19:00:00', 'Salón Principal', 'Pasos básicos y postura'),
(1, 2, 'Salsa Básica - Tiempo y Ritmo', '2025-08-12 19:00:00', 'Salón Principal', 'Encontrar el tiempo musical'),
(2, 3, 'Bachata - Movimientos de Cadera', '2025-08-11 20:00:00', 'Salón Secundario', 'Técnica de cadera en bachata'),
(3, 4, 'Reggaeton - Coreografía Avanzada', '2025-08-13 21:00:00', 'Salón Principal', 'Secuencias complejas');