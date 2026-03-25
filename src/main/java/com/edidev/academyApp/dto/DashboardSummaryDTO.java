package com.edidev.academyApp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO para el resumen del Dashboard de administrador
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardSummaryDTO {
    
    // Resumen de estudiantes
    private Long totalStudents;
    private Long activeStudents;
    private Long newStudentsThisMonth;
    
    // Resumen de cursos
    private Long totalCourses;
    private Long activeCourses;
    
    // Resumen de inscripciones
    private Long totalEnrollments;
    private Long activeEnrollments;
    
    // Resumen financiero
    private BigDecimal monthlyRevenue;
    private BigDecimal yearlyRevenue;
    private Long paymentsThisMonth;
    private BigDecimal pendingPayments;
    
    // Resumen académico
    private Long upcomingSessionsThisWeek;
    private Long completedSessionsThisMonth;
    private Double averageAttendanceRate;
    
    // Próximas sesiones
    private List<ClassSessionDTO> upcomingSessions;
    
    // Alertas y avisos
    private Long studentsWithLowAttendance;
    private Long pendingPaymentsCount;
}
