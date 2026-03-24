package com.edidev.academyApp.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnrollmentRequestDTO {

    @NotNull(message = "El ID del curso es obligatorio")
    private Long courseId;

    @NotNull(message = "Las horas compradas son obligatorias")
    @Positive(message = "Las horas compradas deben ser positivas")
    private BigDecimal purchasedHours;

    @NotNull(message = "El total pagado es obligatorio")
    @Positive(message = "El total pagado debe ser positivo")
    private BigDecimal totalPaid;

    private String notes;
}