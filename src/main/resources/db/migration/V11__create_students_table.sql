CREATE TABLE IF NOT EXISTS students (
    id BIGSERIAL NOT NULL,

    -- Información personal
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    phone VARCHAR(20),
    emergency_contact VARCHAR(150),
    emergency_phone VARCHAR(20),
    date_of_birth DATE,
    address VARCHAR(500),

    -- Información académica
    category VARCHAR(30) NOT NULL DEFAULT 'REGULAR' CHECK (category IN ('REGULAR', 'UNIVERSITY', 'COUPLE', 'SENIOR', 'CHILD')),
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'INACTIVE', 'SUSPENDED', 'GRADUATED', 'DROPPED_OUT', 'ON_HOLD')),
    university_name VARCHAR(200),
    student_id VARCHAR(50),
    career VARCHAR(150),
    semester INTEGER,

    -- Información médica/física
    medical_conditions TEXT,
    allergies VARCHAR(500),
    medications TEXT,
    dance_experience VARCHAR(500),
    fitness_level VARCHAR(100),
    physical_limitations TEXT,

    -- Preferencias de contacto
    preferred_contact_method VARCHAR(50) DEFAULT 'EMAIL',
    newsletter_subscription BOOLEAN DEFAULT TRUE,
    promotional_emails BOOLEAN DEFAULT TRUE,
    notes TEXT,

    -- Relación con usuario
    user_id BIGINT,

    -- Auditoría
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),

    PRIMARY KEY (id),
    CONSTRAINT fk_students_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_student_email ON students(email);
CREATE INDEX IF NOT EXISTS idx_student_phone ON students(phone);
CREATE INDEX IF NOT EXISTS idx_student_status ON students(status);
CREATE INDEX IF NOT EXISTS idx_student_category ON students(category);
CREATE INDEX IF NOT EXISTS idx_student_user_id ON students(user_id);

-- Insertar algunos estudiantes de ejemplo
INSERT INTO students (
    first_name, last_name, email, phone, category, status,
    university_name, dance_experience, fitness_level, created_by
) VALUES
(
    'Ana', 'García', 'ana.garcia@email.com', '+56912345678',
    'UNIVERSITY', 'ACTIVE', 'Universidad de Chile',
    'Principiante en salsa', 'Intermedio', 'SYSTEM'
),
(
    'Carlos', 'Rodríguez', 'carlos.rodriguez@email.com', '+56987654321',
    'REGULAR', 'ACTIVE', NULL,
    '2 años de bachata', 'Avanzado', 'SYSTEM'
),
(
    'María', 'López', 'maria.lopez@email.com', '+56911223344',
    'UNIVERSITY', 'ACTIVE', 'Pontificia Universidad Católica',
    'Sin experiencia', 'Principiante', 'SYSTEM'
);