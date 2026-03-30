package com.edidev.academyApp.dto;

import com.edidev.academyApp.enums.ProductSize;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductVariantDTO {
    private Long id;
    private Long productId;
    private ProductSize size;
    private String color;
    private String sku;
    private Integer stock;
    private BigDecimal additionalPrice;
    private Boolean isActive;
    private LocalDateTime createdAt;
}
