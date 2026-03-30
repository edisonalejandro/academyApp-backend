-- ===========================================
-- V2: Reglas de Precios (Pricing Rules)
-- ===========================================
-- Tabla de reglas de precios: motor de tarifas por categoría, cantidad de clases y número de personas.
-- Los datos de seed son gestionados por DataInitializer en tiempo de ejecución.

CREATE TABLE IF NOT EXISTS pricing_rules (
    id                  BIGSERIAL      PRIMARY KEY,
    name                VARCHAR(100)   NOT NULL,
    description         VARCHAR(500),
    pricing_type        VARCHAR(50)    NOT NULL
        CHECK (pricing_type IN ('SINGLE_CLASS', 'PACKAGE_4', 'PACKAGE_8', 'PACKAGE_12',
                                'COUPLE_PACKAGE_4', 'COUPLE_PACKAGE_8', 'COUPLE_PACKAGE_12',
                                'MONTHLY', 'CUSTOM')),
    student_category    VARCHAR(30)    NOT NULL
        CHECK (student_category IN ('REGULAR', 'UNIVERSITY', 'COUPLE', 'SENIOR', 'CHILD')),
    person_count        INTEGER        NOT NULL DEFAULT 1,
    class_quantity      INTEGER        NOT NULL,
    price               DECIMAL(10,2)  NOT NULL,
    discount_percentage DECIMAL(5,2)   DEFAULT 0.00,
    final_price         DECIMAL(10,2)  NOT NULL,
    valid_from          TIMESTAMP,
    valid_until         TIMESTAMP,
    is_active           BOOLEAN        NOT NULL DEFAULT TRUE,
    created_at          TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP      DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_pricing_rules_type       ON pricing_rules(pricing_type);
CREATE INDEX IF NOT EXISTS idx_pricing_rules_category   ON pricing_rules(student_category);
CREATE INDEX IF NOT EXISTS idx_pricing_rules_active     ON pricing_rules(is_active);
CREATE INDEX IF NOT EXISTS idx_pricing_rules_final_price ON pricing_rules(final_price);
CREATE INDEX IF NOT EXISTS idx_pricing_rules_category_person_active
    ON pricing_rules(student_category, person_count, is_active);
