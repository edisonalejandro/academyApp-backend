package com.edidev.academyApp.dto;

import com.edidev.academyApp.enums.PaymentMethod;
import com.edidev.academyApp.enums.StudentCategory;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentRequestDTO {

    // ========== CAMPOS NUEVOS - SISTEMA FLEXIBLE ==========
    
    @NotNull(message = "El ID del curso es obligatorio")
    private Long courseId;

    @NotNull(message = "El ID de la regla de precios es obligatorio")
    private Long pricingRuleId;

    @NotNull(message = "La categoría de estudiante es obligatoria")
    private StudentCategory studentCategory;

    @NotNull(message = "El método de pago es obligatorio")
    private PaymentMethod paymentMethod;

    @Min(value = 1, message = "El número de personas debe ser al menos 1")
    @Max(value = 2, message = "El número máximo de personas es 2")
    @Builder.Default
    private Integer personCount = 1;

    private String notes;

    // Para pagos con tarjeta o transferencia
    private String transactionId;

    // Método helper
    public boolean isCouple() {
        return personCount == 2;
    }
}