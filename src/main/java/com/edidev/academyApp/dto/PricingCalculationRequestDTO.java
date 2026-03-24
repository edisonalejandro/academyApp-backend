package com.edidev.academyApp.dto;

import com.edidev.academyApp.enums.StudentCategory;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PricingCalculationRequestDTO {

    @NotNull(message = "El ID del curso es obligatorio")
    private Long courseId;

    @NotNull(message = "La categoría de estudiante es obligatoria")
    private StudentCategory studentCategory;

    @Min(value = 1, message = "El número de personas debe ser al menos 1")
    @Max(value = 2, message = "El número máximo de personas es 2")
    @Builder.Default
    private Integer personCount = 1;

    // Método helper
    public boolean isCouple() {
        return personCount == 2;
    }
}