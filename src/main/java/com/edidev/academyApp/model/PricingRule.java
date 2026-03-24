package com.edidev.academyApp.model;

import com.edidev.academyApp.enums.PricingType;
import com.edidev.academyApp.enums.StudentCategory;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "pricing_rules")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(exclude = {"payments"})
@ToString(exclude = {"payments"})
public class PricingRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description", length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "pricing_type", nullable = false)
    private PricingType pricingType;

    @Enumerated(EnumType.STRING)
    @Column(name = "student_category", nullable = false)
    private StudentCategory studentCategory;

    @Column(name = "person_count", nullable = false)
    private Integer personCount;

    @Column(name = "class_quantity", nullable = false)
    private Integer classQuantity;

    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "discount_percentage", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal discountPercentage = BigDecimal.ZERO;

    // ✅ CAMPO QUE FALTABA
    @Column(name = "final_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal finalPrice;

    @Column(name = "valid_from")
    private LocalDateTime validFrom;

    @Column(name = "valid_until")
    private LocalDateTime validUntil;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        
        // Calcular precio final automáticamente si no está definido
        if (finalPrice == null) {
            finalPrice = calculateFinalPrice();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        
        // Recalcular precio final si el precio o descuento cambiaron
        finalPrice = calculateFinalPrice();
    }

    /**
     * Calcula el precio final aplicando descuentos
     */
    private BigDecimal calculateFinalPrice() {
        if (price == null) {
            return BigDecimal.ZERO;
        }
        
        if (discountPercentage == null || discountPercentage.compareTo(BigDecimal.ZERO) <= 0) {
            return price;
        }

        BigDecimal discount = price.multiply(discountPercentage)
                .divide(BigDecimal.valueOf(100), 2, BigDecimal.ROUND_HALF_UP);
        
        return price.subtract(discount);
    }

    /**
     * Obtiene el precio por clase
     */
    public BigDecimal getPricePerClass() {
        if (classQuantity == null || classQuantity == 0) {
            return BigDecimal.ZERO;
        }
        return finalPrice.divide(BigDecimal.valueOf(classQuantity), 2, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * Obtiene el ahorro total
     */
    public BigDecimal getTotalSavings() {
        return price.subtract(finalPrice);
    }

    /**
     * Obtiene el porcentaje de ahorro real
     */
    public BigDecimal getSavingsPercentage() {
        if (price.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        
        return getTotalSavings()
                .divide(price, 4, BigDecimal.ROUND_HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }

    /**
     * Verifica si la regla está vigente actualmente
     */
    public boolean isCurrentlyValid() {
        if (!isActive) {
            return false;
        }

        LocalDateTime now = LocalDateTime.now();
        
        if (validFrom != null && now.isBefore(validFrom)) {
            return false;
        }

        if (validUntil != null && now.isAfter(validUntil)) {
            return false;
        }

        return true;
    }

    /**
     * Método para establecer precio y recalcular automáticamente
     */
    public void setPriceAndRecalculate(BigDecimal newPrice, BigDecimal newDiscountPercentage) {
        this.price = newPrice;
        this.discountPercentage = newDiscountPercentage != null ? newDiscountPercentage : BigDecimal.ZERO;
        this.finalPrice = calculateFinalPrice();
    }
}