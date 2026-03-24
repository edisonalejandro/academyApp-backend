package com.edidev.academyApp.dto;

import com.edidev.academyApp.enums.PaymentMethod;
import com.edidev.academyApp.enums.PaymentStatus;
import com.edidev.academyApp.enums.PricingType;
import com.edidev.academyApp.enums.StudentCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentResponseDTO {
    
    private Long id;
    private String paymentCode;
    private String studentName;
    private String courseName;
    private PricingType pricingType;
    private StudentCategory studentCategory;
    private Integer quantityClasses;
    private Integer personCount;
    private BigDecimal originalPrice;
    private BigDecimal discountAmount;
    private BigDecimal finalPrice;
    private BigDecimal pricePerClass;
    private PaymentMethod paymentMethod;
    private PaymentStatus status;
    private String transactionId;
    private LocalDateTime paymentDate;
    private LocalDateTime createdAt;
    private String notes;
}