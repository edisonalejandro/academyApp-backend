package com.edidev.academyApp.dto;

import com.edidev.academyApp.enums.ProductSize;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItemDTO {
    private Long id;
    private Long variantId;
    private Long productId;
    private String productName;
    private String productImageUrl;
    private String categoryName;
    private ProductSize size;
    private String color;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal subtotal;
    private Integer availableStock;
    private LocalDateTime addedAt;
}
