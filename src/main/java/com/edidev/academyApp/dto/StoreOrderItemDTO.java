package com.edidev.academyApp.dto;

import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StoreOrderItemDTO {
    private Long id;
    private Long variantId;
    private String productName;
    private String variantSize;
    private String variantColor;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal subtotal;
}
