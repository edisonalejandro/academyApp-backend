package com.edidev.academyApp.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddToCartRequest {
    @NotNull(message = "El ID de variante es obligatorio")
    private Long variantId;

    @NotNull
    @Min(value = 1, message = "La cantidad debe ser al menos 1")
    private Integer quantity;
}
