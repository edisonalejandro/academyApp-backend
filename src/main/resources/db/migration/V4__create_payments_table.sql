-- ===========================================
-- V4: Pagos
-- ===========================================
-- Depende de: V1 (users), V2 (pricing_rules), V3 (courses)

CREATE TABLE IF NOT EXISTS payments (
    id                BIGSERIAL     PRIMARY KEY,
    user_id           BIGINT        NOT NULL,
    course_id         BIGINT        NOT NULL,
    pricing_rule_id   BIGINT        NOT NULL,
    payment_code      VARCHAR(50)   UNIQUE NOT NULL,
    pricing_type      VARCHAR(50)   NOT NULL
        CHECK (pricing_type IN ('SINGLE_CLASS','PACKAGE_4','PACKAGE_8','PACKAGE_12',
                                'COUPLE_PACKAGE_4','COUPLE_PACKAGE_8','COUPLE_PACKAGE_12',
                                'MONTHLY','CUSTOM')),
    student_category  VARCHAR(30)   NOT NULL
        CHECK (student_category IN ('REGULAR','UNIVERSITY','COUPLE','SENIOR','CHILD')),
    quantity_classes  INTEGER       NOT NULL,
    person_count      INTEGER       NOT NULL DEFAULT 1,
    original_price    DECIMAL(10,2) NOT NULL,
    discount_amount   DECIMAL(10,2) DEFAULT 0.00,
    final_price       DECIMAL(10,2) NOT NULL,
    payment_method    VARCHAR(30)   NOT NULL
        CHECK (payment_method IN ('CASH','BANK_TRANSFER','CREDIT_CARD','DEBIT_CARD','MERCADO_PAGO','OTHER')),
    status            VARCHAR(30)   NOT NULL DEFAULT 'PENDING'
        CHECK (status IN ('PENDING','COMPLETED','FAILED','REFUNDED','CANCELLED')),
    transaction_id    VARCHAR(100),
    payment_date      TIMESTAMP,
    notes             TEXT,
    created_at        TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP     DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_payment_user         FOREIGN KEY (user_id)         REFERENCES users(id)          ON DELETE RESTRICT,
    CONSTRAINT fk_payment_course       FOREIGN KEY (course_id)       REFERENCES courses(id)        ON DELETE RESTRICT,
    CONSTRAINT fk_payment_pricing_rule FOREIGN KEY (pricing_rule_id) REFERENCES pricing_rules(id)  ON DELETE RESTRICT
);

CREATE INDEX IF NOT EXISTS idx_payments_user         ON payments(user_id);
CREATE INDEX IF NOT EXISTS idx_payments_course       ON payments(course_id);
CREATE INDEX IF NOT EXISTS idx_payments_status       ON payments(status);
CREATE INDEX IF NOT EXISTS idx_payments_payment_date ON payments(payment_date);
CREATE INDEX IF NOT EXISTS idx_payments_code         ON payments(payment_code);
