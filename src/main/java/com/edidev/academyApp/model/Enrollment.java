package com.edidev.academyApp.model;

import com.edidev.academyApp.enums.EnrollmentStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "enrollments", indexes = {
    @Index(name = "idx_enrollment_student_id", columnList = "student_id"),
    @Index(name = "idx_enrollment_course_id", columnList = "course_id"),
    @Index(name = "idx_enrollment_status", columnList = "status"),
    @Index(name = "idx_enrollment_date", columnList = "enrollment_date"),
    @Index(name = "idx_enrollment_student_course", columnList = "student_id, course_id", unique = true)
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(exclude = {"student", "course", "payment"})
@ToString(exclude = {"student", "course", "payment"})
public class Enrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Nueva relación con Student (preferida)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id")
    private Payment payment;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private EnrollmentStatus status = EnrollmentStatus.PENDING;

    @Column(name = "enrollment_date", nullable = false)
    private LocalDateTime enrollmentDate;

    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    @Column(name = "completion_date")
    private LocalDateTime completionDate;

    // Horas compradas (para tracking de clases)
    @Column(name = "purchased_hours", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal purchasedHours = BigDecimal.ZERO;

    // Horas utilizadas
    @Column(name = "used_hours", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal usedHours = BigDecimal.ZERO;

    // Total pagado por el enrollment
    @Column(name = "total_paid", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal totalPaid = BigDecimal.ZERO;

    // Precio pagado por este enrollment específico
    @Column(name = "paid_amount", precision = 10, scale = 2)
    private BigDecimal paidAmount;

    // Descuento aplicado
    @Column(name = "discount_percentage", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal discountPercentage = BigDecimal.ZERO;

    @Column(name = "final_price", precision = 10, scale = 2)
    private BigDecimal finalPrice;

    // Notas adicionales
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    // Información de cancelación
    @Column(name = "cancellation_reason", length = 500)
    private String cancellationReason;

    @Column(name = "cancelled_date")
    private LocalDateTime cancelledDate;

    @Column(name = "cancelled_by", length = 100)
    private String cancelledBy;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    // ========== MÉTODOS DE CONVENIENCIA ==========

    /**
     * Verifica si la inscripción está activa
     */
    public boolean isActive() {
        return status == EnrollmentStatus.ACTIVE;
    }

    /**
     * Verifica si la inscripción está completada
     */
    public boolean isCompleted() {
        return status == EnrollmentStatus.COMPLETED;
    }

    /**
     * Verifica si la inscripción está cancelada
     */
    public boolean isCancelled() {
        return status == EnrollmentStatus.CANCELLED;
    }

    /**
     * Obtiene las horas restantes
     */
    public BigDecimal getRemainingHours() {
        if (purchasedHours == null || usedHours == null) {
            return BigDecimal.ZERO;
        }
        return purchasedHours.subtract(usedHours);
    }

    /**
     * Método de conveniencia para acceder al User a través del Student
     * (para compatibilidad con código existente)
     */
    public User getUser() {
        return student != null ? student.getUser() : null;
    }

    /**
     * Activa la inscripción
     */
    public void activate(String activatedBy) {
        this.status = EnrollmentStatus.ACTIVE;
        this.startDate = LocalDateTime.now();
        this.updatedBy = activatedBy;
    }

    /**
     * Completa la inscripción
     */
    public void complete(String completedBy) {
        this.status = EnrollmentStatus.COMPLETED;
        this.completionDate = LocalDateTime.now();
        this.endDate = LocalDateTime.now();
        this.updatedBy = completedBy;
    }

    /**
     * Cancela la inscripción
     */
    public void cancel(String reason, String cancelledBy) {
        this.status = EnrollmentStatus.CANCELLED;
        this.cancellationReason = reason;
        this.cancelledDate = LocalDateTime.now();
        this.cancelledBy = cancelledBy;
        this.updatedBy = cancelledBy;
    }

    /**
     * Obtiene información para logs
     */
    public String getLogSummary() {
        return String.format("Enrollment[id=%d, student=%s, course=%s, status=%s]",
                id, 
                student != null ? student.getEmail() : "N/A", 
                course != null ? course.getTitle() : "N/A", 
                status);
    }

    @PrePersist
    protected void onCreate() {
        if (enrollmentDate == null) {
            enrollmentDate = LocalDateTime.now();
        }
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}