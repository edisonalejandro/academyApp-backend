package com.edidev.academyApp.dto;

import java.math.BigDecimal;

    // DTO para resumen de inscripciones
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public class EnrollmentSummaryDTO {
        private int totalEnrollments;
        private int activeEnrollments;
        private BigDecimal totalHoursPurchased;
        private BigDecimal totalHoursUsed;
        private BigDecimal totalHoursRemaining;
        private BigDecimal totalAmountPaid;
    }
