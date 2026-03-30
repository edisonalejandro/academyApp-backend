package com.edidev.academyApp.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDTO {
    private Long id;
    private Long categoryId;
    private String categoryName;
    private String categorySlug;
    private String name;
    private String slug;
    private String description;
    private BigDecimal basePrice;
    private String imageUrl;
    private Boolean isActive;
    private Boolean featured;
    private List<ProductVariantDTO> variants;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
