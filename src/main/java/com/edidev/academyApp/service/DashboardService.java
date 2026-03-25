package com.edidev.academyApp.service;

import com.edidev.academyApp.dto.ClassSessionDTO;
import com.edidev.academyApp.dto.DashboardSummaryDTO;
import com.edidev.academyApp.enums.ClassStatus;
import com.edidev.academyApp.enums.EnrollmentStatus;
import com.edidev.academyApp.enums.PaymentStatus;
import com.edidev.academyApp.enums.StudentStatus;
import com.edidev.academyApp.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio para generar resúmenes y estadísticas del Dashboard
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class DashboardService {

    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final PaymentRepository paymentRepository;
    private final ClassSessionRepository classSessionRepository;
    private final AttendanceRepository attendanceRepository;
    private final ClassSessionService classSessionService;

    /**
     * Obtiene el resumen completo del dashboard
     */
    public DashboardSummaryDTO getDashboardSummary() {
        log.info("Generando resumen completo del dashboard");

        // Fechas para filtros
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfMonth = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime endOfMonth = now.withDayOfMonth(now.toLocalDate().lengthOfMonth())
                .withHour(23).withMinute(59).withSecond(59);
        LocalDateTime startOfYear = now.withDayOfYear(1).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime endOfWeek = now.plusWeeks(1);

        // Estudiantes
        Long totalStudents = studentRepository.count();
        Long activeStudents = studentRepository.countByStatus(StudentStatus.ACTIVE);
        Long newStudentsThisMonth = studentRepository.countByCreatedAtBetween(startOfMonth, now);

        // Cursos
        Long totalCourses = courseRepository.count();
        Long activeCourses = courseRepository.countByIsActiveTrue();

        // Inscripciones
        Long totalEnrollments = enrollmentRepository.count();
        Long activeEnrollments = enrollmentRepository.countByStatus(EnrollmentStatus.ACTIVE);

        // Pagos y finanzas
        BigDecimal monthlyRevenue = paymentRepository.sumAmountByStatusAndDateBetween(
                PaymentStatus.COMPLETED, startOfMonth, endOfMonth);
        BigDecimal yearlyRevenue = paymentRepository.sumAmountByStatusAndDateBetween(
                PaymentStatus.COMPLETED, startOfYear, now);
        Long paymentsThisMonth = paymentRepository.countByStatusAndPaymentDateBetween(
                PaymentStatus.COMPLETED, startOfMonth, endOfMonth);
        BigDecimal pendingPayments = paymentRepository.sumAmountByStatus(PaymentStatus.PENDING);
        Long pendingPaymentsCount = paymentRepository.countByStatus(PaymentStatus.PENDING);

        // Sesiones
        Long upcomingSessionsThisWeek = classSessionRepository.countByStatusAndScheduledDateBetween(
                ClassStatus.SCHEDULED, now, endOfWeek);
        Long completedSessionsThisMonth = classSessionRepository.countByStatusAndScheduledDateBetween(
                ClassStatus.COMPLETED, startOfMonth, now);

        // Asistencias
        Double averageAttendanceRate = attendanceRepository.calculateOverallAttendanceRate();

        // Próximas 5 sesiones
        List<ClassSessionDTO> upcomingSessions = classSessionRepository
                .findUpcomingSessionsByDateRange(now, now.plusWeeks(2))
                .stream()
                .limit(5)
                .map(classSessionService::toDTO)
                .collect(Collectors.toList());

        // Alertas
        Long studentsWithLowAttendance = (long) attendanceRepository
                .findStudentsWithLowAttendance(50.0)
                .size();

        return DashboardSummaryDTO.builder()
                .totalStudents(totalStudents)
                .activeStudents(activeStudents)
                .newStudentsThisMonth(newStudentsThisMonth)
                .totalCourses(totalCourses)
                .activeCourses(activeCourses)
                .totalEnrollments(totalEnrollments)
                .activeEnrollments(activeEnrollments)
                .monthlyRevenue(monthlyRevenue != null ? monthlyRevenue : BigDecimal.ZERO)
                .yearlyRevenue(yearlyRevenue != null ? yearlyRevenue : BigDecimal.ZERO)
                .paymentsThisMonth(paymentsThisMonth)
                .pendingPayments(pendingPayments != null ? pendingPayments : BigDecimal.ZERO)
                .pendingPaymentsCount(pendingPaymentsCount)
                .upcomingSessionsThisWeek(upcomingSessionsThisWeek)
                .completedSessionsThisMonth(completedSessionsThisMonth)
                .averageAttendanceRate(averageAttendanceRate != null ? averageAttendanceRate : 0.0)
                .upcomingSessions(upcomingSessions)
                .studentsWithLowAttendance(studentsWithLowAttendance)
                .build();
    }

    /**
     * Obtiene estadísticas rápidas (versión simplificada)
     * Para profesores que no necesitan ver toda la información financiera
     */
    public DashboardSummaryDTO getQuickStats() {
        log.info("Generando estadísticas rápidas");

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endOfWeek = now.plusWeeks(1);

        Long activeStudents = studentRepository.countByStatus(StudentStatus.ACTIVE);
        Long activeCourses = courseRepository.countByIsActiveTrue();
        Long activeEnrollments = enrollmentRepository.countByStatus(EnrollmentStatus.ACTIVE);
        Long upcomingSessionsThisWeek = classSessionRepository.countByStatusAndScheduledDateBetween(
                ClassStatus.SCHEDULED, now, endOfWeek);

        return DashboardSummaryDTO.builder()
                .activeStudents(activeStudents)
                .activeCourses(activeCourses)
                .activeEnrollments(activeEnrollments)
                .upcomingSessionsThisWeek(upcomingSessionsThisWeek)
                .build();
    }
}
