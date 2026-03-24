package com.edidev.academyApp.model;

import com.edidev.academyApp.enums.PaymentMethod;
import com.edidev.academyApp.enums.PaymentStatus;
import com.edidev.academyApp.enums.PricingType;
import com.edidev.academyApp.enums.StudentCategory;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "payments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Referencia al usuario que realiza el pago
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Referencia al curso
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    // Referencia a la regla de precios utilizada
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pricing_rule_id", nullable = false)
    private PricingRule pricingRule;

    @Column(name = "payment_code", unique = true, nullable = false)
    private String paymentCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "pricing_type", nullable = false)
    private PricingType pricingType;

    @Enumerated(EnumType.STRING)
    @Column(name = "student_category", nullable = false)
    private StudentCategory studentCategory;

    @Column(name = "quantity_classes", nullable = false)
    private Integer quantityClasses;

    @Column(name = "person_count", nullable = false)
    @Builder.Default
    private Integer personCount = 1;

    // Precio original antes de descuentos
    @Column(name = "original_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal originalPrice;

    // Descuento aplicado
    @Column(name = "discount_amount", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal discountAmount = BigDecimal.ZERO;

    // Precio final pagado
    @Column(name = "final_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal finalPrice;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private PaymentStatus status = PaymentStatus.PENDING;

    @Column(name = "transaction_id")
    private String transactionId;

    @Column(name = "payment_date")
    private LocalDateTime paymentDate;

    @Column(name = "notes")
    private String notes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ✅ Agregar relación con enrollments si no existe
    @OneToMany(mappedBy = "payment", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Enrollment> enrollments = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (paymentCode == null) {
            paymentCode = generatePaymentCode();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    private String generatePaymentCode() {
        return "PAY-" + System.currentTimeMillis();
    }

    /**
     * Calcula el precio por clase
     */
    public BigDecimal getPricePerClass() {
        return finalPrice.divide(BigDecimal.valueOf(quantityClasses), 2, BigDecimal.ROUND_HALF_UP);
    }
}