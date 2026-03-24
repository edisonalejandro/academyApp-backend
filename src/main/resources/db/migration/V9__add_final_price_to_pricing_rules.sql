-- Agregar la columna final_price
ALTER TABLE pricing_rules
ADD COLUMN IF NOT EXISTS final_price DECIMAL(10,2);

-- Calcular el precio final para las reglas existentes
UPDATE pricing_rules
SET final_price = CASE
    WHEN discount_percentage IS NULL OR discount_percentage = 0 THEN price
    ELSE price - (price * discount_percentage / 100)
END;

-- Hacer la columna NOT NULL después de llenar los datos
ALTER TABLE pricing_rules
    ALTER COLUMN final_price SET NOT NULL;

-- Agregar índice para mejorar performance en consultas por precio final
CREATE INDEX IF NOT EXISTS idx_pricing_rules_final_price ON pricing_rules(final_price);

-- Agregar índice compuesto para consultas frecuentes
CREATE INDEX IF NOT EXISTS idx_pricing_rules_category_person_active ON pricing_rules(student_category, person_count, is_active);