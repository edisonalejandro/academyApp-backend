-- Reemplazar el esquema anterior de enrollments (V7) con el esquema actualizado
DROP TABLE IF EXISTS enrollments CASCADE;

CREATE TABLE enrollments (
    id BIGSERIAL NOT NULL,
    student_id BIGINT NOT NULL,
    course_id BIGINT NOT NULL,
    payment_id BIGINT,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'ACTIVE', 'COMPLETED', 'CANCELLED', 'SUSPENDED', 'TRANSFERRED', 'HOURS_EXHAUSTED')),
    enrollment_date TIMESTAMP NOT NULL,
    start_date TIMESTAMP,
    end_date TIMESTAMP,
    completion_date TIMESTAMP,
    purchased_hours DECIMAL(5,2) DEFAULT 0.00,
    used_hours DECIMAL(5,2) DEFAULT 0.00,
    total_paid DECIMAL(10,2) DEFAULT 0.00,
    paid_amount DECIMAL(10,2),
    discount_percentage DECIMAL(5,2) DEFAULT 0.00,
    final_price DECIMAL(10,2),
    notes TEXT,
    cancellation_reason VARCHAR(500),
    cancelled_date TIMESTAMP,
    cancelled_by VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),

    PRIMARY KEY (id),
    CONSTRAINT fk_enrollment_student FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
    CONSTRAINT fk_enrollment_course FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE,
    CONSTRAINT fk_enrollment_payment FOREIGN KEY (payment_id) REFERENCES payments(id) ON DELETE SET NULL,
    CONSTRAINT uq_enrollment_student_course UNIQUE (student_id, course_id)
);

CREATE INDEX IF NOT EXISTS idx_enrollment_student_id ON enrollments(student_id);
CREATE INDEX IF NOT EXISTS idx_enrollment_course_id ON enrollments(course_id);
CREATE INDEX IF NOT EXISTS idx_enrollment_status ON enrollments(status);
CREATE INDEX IF NOT EXISTS idx_enrollment_date ON enrollments(enrollment_date);

-- Insertar algunos enrollments de ejemplo (ajustar IDs según tus datos)
INSERT INTO enrollments (
    student_id, course_id, status, enrollment_date,
    paid_amount, final_price, created_by
) VALUES
(1, 1, 'ACTIVE', NOW(), 24000.00, 24000.00, 'SYSTEM'),
(2, 1, 'ACTIVE', NOW(), 40000.00, 40000.00, 'SYSTEM'),
(3, 2, 'PENDING', NOW(), 14000.00, 14000.00, 'SYSTEM');