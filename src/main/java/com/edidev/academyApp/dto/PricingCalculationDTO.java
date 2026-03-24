package com.edidev.academyApp.dto;

import com.edidev.academyApp.enums.PricingType;
import com.edidev.academyApp.enums.StudentCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PricingCalculationDTO {

    private Long courseId;
    private String courseName;
    private StudentCategory studentCategory;
    private Integer personCount;
    private Boolean isCouple;
    private List<PricingOption> options;
    private Long recommendedOptionId;
    private LocalDateTime calculatedAt;
    
    // Indica si se usaron precios por defecto (cuando no hay reglas configuradas)
    @Builder.Default
    private Boolean hasDefaultPricing = false;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PricingOption {
        private Long pricingRuleId;
        private String name;
        private String description;
        private PricingType pricingType;
        private Integer classQuantity;
        private BigDecimal originalPrice;
        private BigDecimal finalPrice;
        private BigDecimal discountPercentage;
        private BigDecimal savings;
        private BigDecimal pricePerClass;
        private Boolean isRecommended;
        private LocalDateTime validFrom;
        private LocalDateTime validUntil;
        
        // Método helper para mostrar el ahorro como porcentaje
        public Double getSavingsPercentage() {
            if (originalPrice.compareTo(BigDecimal.ZERO) == 0) {
                return 0.0;
            }
            return savings.divide(originalPrice, 4, BigDecimal.ROUND_HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .doubleValue();
        }
    }
}
