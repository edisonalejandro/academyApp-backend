package com.edidev.academyApp.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartDTO {
    private Long id;
    private Long userId;
    private List<CartItemDTO> items;
    private Integer totalItems;
    private BigDecimal subtotal;
    private LocalDateTime updatedAt;
}
