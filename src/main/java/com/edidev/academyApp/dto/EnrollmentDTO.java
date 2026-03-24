package com.edidev.academyApp.dto;

import com.edidev.academyApp.enums.EnrollmentStatus;
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
public class EnrollmentDTO {

    private Long id;
    private Long studentId;
    private String studentName;
    private String studentEmail;
    private Long courseId;
    private String courseName;
    private String courseCode;
    private String courseDescription;
    private Long paymentId;
    private String paymentCode;
    private EnrollmentStatus status;
    private LocalDateTime enrollmentDate;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private LocalDateTime completionDate;
    private BigDecimal paidAmount;
    private BigDecimal discountPercentage;
    private BigDecimal finalPrice;
    
    // Campos de gestión de horas (compatibilidad con sistema anterior)
    private BigDecimal purchasedHours;
    private BigDecimal usedHours;
    private BigDecimal remainingHours;
    private BigDecimal totalPaid;
    
    private String notes;
    private String cancellationReason;
    private LocalDateTime cancelledDate;
    private String cancelledBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;

    // Campos calculados
    private Integer totalClasses;
    private Integer attendedClasses;
    private Integer remainingClasses;
    private Double attendancePercentage;
    private Boolean canCancel;
    private Boolean isActive;
    private Boolean isCompleted;
    private Long daysUntilStart;
    private Long daysInProgress;
}