package com.edidev.academyApp.dto;

import com.edidev.academyApp.enums.PricingType;
import com.edidev.academyApp.enums.StudentCategory;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PricingRuleDTO {

    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
    private String name;

    @Size(max = 500, message = "La descripción no puede exceder 500 caracteres")
    private String description;

    @NotNull(message = "El tipo de precio es obligatorio")
    private PricingType pricingType;

    @NotNull(message = "La categoría de estudiante es obligatoria")
    private StudentCategory studentCategory;

    @NotNull(message = "El número de personas es obligatorio")
    @Min(value = 1, message = "El número de personas debe ser al menos 1")
    @Max(value = 2, message = "El número máximo de personas es 2")
    private Integer personCount;

    @NotNull(message = "La cantidad de clases es obligatoria")
    @Min(value = 1, message = "La cantidad de clases debe ser al menos 1")
    private Integer classQuantity;

    @NotNull(message = "El precio es obligatorio")
    @DecimalMin(value = "0.0", inclusive = false, message = "El precio debe ser mayor a 0")
    private BigDecimal price;

    @DecimalMin(value = "0.0", message = "El descuento no puede ser negativo")
    @DecimalMax(value = "100.0", message = "El descuento no puede ser mayor a 100%")
    private BigDecimal discountPercentage;

    private BigDecimal finalPrice;

    private LocalDateTime validFrom;
    private LocalDateTime validUntil;

    @Builder.Default
    private Boolean isActive = true;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Métodos helper
    public boolean isCouple() {
        return personCount == 2;
    }

    public boolean isCurrentlyValid() {
        LocalDateTime now = LocalDateTime.now();
        
        if (validFrom != null && now.isBefore(validFrom)) {
            return false;
        }
        
        if (validUntil != null && now.isAfter(validUntil)) {
            return false;
        }
        
        return isActive;
    }

    public BigDecimal getPricePerClass() {
        if (classQuantity == null || classQuantity == 0) {
            return BigDecimal.ZERO;
        }
        return finalPrice.divide(BigDecimal.valueOf(classQuantity), 2, BigDecimal.ROUND_HALF_UP);
    }
}