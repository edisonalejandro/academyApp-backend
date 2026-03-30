package com.edidev.academyApp.dto;

import com.edidev.academyApp.enums.OrderStatus;
import com.edidev.academyApp.enums.StorePaymentMethod;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StoreOrderDTO {
    private Long id;
    private String orderNumber;
    private Long userId;
    private String customerName;
    private String customerEmail;
    private String customerPhone;
    private OrderStatus status;
    private StorePaymentMethod paymentMethod;
    private BigDecimal subtotal;
    private BigDecimal discountAmount;
    private BigDecimal totalAmount;
    private String notes;
    private String shippingAddress;
    private List<StoreOrderItemDTO> items;
    private LocalDateTime paidAt;
    private LocalDateTime shippedAt;
    private LocalDateTime deliveredAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
