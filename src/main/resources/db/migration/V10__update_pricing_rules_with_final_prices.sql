-- Actualizar las reglas existentes con precios finales correctos
UPDATE pricing_rules 
SET 
    final_price = CASE 
        WHEN pricing_type = 'SINGLE_CLASS' AND student_category = 'REGULAR' THEN 4000
        WHEN pricing_type = 'SINGLE_CLASS' AND student_category = 'UNIVERSITY' THEN 2000
        WHEN pricing_type = 'PACKAGE_4' AND student_category = 'REGULAR' THEN 14000
        WHEN pricing_type = 'PACKAGE_8' AND student_category = 'REGULAR' THEN 24000
        WHEN pricing_type = 'PACKAGE_12' AND student_category = 'REGULAR' THEN 30000
        WHEN pricing_type = 'COUPLE_PACKAGE_8' AND person_count = 2 THEN 40000
        ELSE price - (price * COALESCE(discount_percentage, 0) / 100)
    END,
    updated_at = NOW()
WHERE final_price IS NULL OR final_price = 0;

-- Verificar que todos los registros tienen precio final
SELECT 
    id, 
    name, 
    price, 
    discount_percentage, 
    final_price,
    (price - final_price) as savings,
    ROUND(((price - final_price) / price) * 100, 2) as savings_percentage
FROM pricing_rules 
ORDER BY student_category, person_count, class_quantity;