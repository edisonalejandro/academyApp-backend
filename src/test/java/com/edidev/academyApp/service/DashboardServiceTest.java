package com.edidev.academyApp.service;

import com.edidev.academyApp.dto.DashboardSummaryDTO;
import com.edidev.academyApp.enums.ClassStatus;
import com.edidev.academyApp.enums.EnrollmentStatus;
import com.edidev.academyApp.enums.PaymentStatus;
import com.edidev.academyApp.enums.StudentStatus;
import com.edidev.academyApp.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private ClassSessionRepository classSessionRepository;

    @Mock
    private AttendanceRepository attendanceRepository;

    @Mock
    private ClassSessionService classSessionService;

    @InjectMocks
    private DashboardService dashboardService;

    @BeforeEach
    void setUp() {
        // No pre-configurar mocks en setUp para evitar stubbings innecesarios
        // Cada test configurará solo lo que necesite
    }

    @Test
    void testGetDashboardSummary() {
        // Given
        when(studentRepository.count()).thenReturn(100L);
        when(studentRepository.countByStatus(StudentStatus.ACTIVE)).thenReturn(85L);
        when(courseRepository.count()).thenReturn(15L);
        when(courseRepository.countByIsActiveTrue()).thenReturn(12L);
        when(enrollmentRepository.count()).thenReturn(200L);
        when(enrollmentRepository.countByStatus(EnrollmentStatus.ACTIVE)).thenReturn(180L);
        when(studentRepository.countByCreatedAtBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(10L);
        when(paymentRepository.sumAmountByStatusAndDateBetween(
                eq(PaymentStatus.COMPLETED),
                any(LocalDateTime.class),
                any(LocalDateTime.class)))
                .thenReturn(new BigDecimal("50000.00"));
        when(paymentRepository.countByStatusAndPaymentDateBetween(
                eq(PaymentStatus.COMPLETED),
                any(LocalDateTime.class),
                any(LocalDateTime.class)))
                .thenReturn(45L);
        when(paymentRepository.sumAmountByStatus(PaymentStatus.PENDING))
                .thenReturn(new BigDecimal("5000.00"));
        when(paymentRepository.countByStatus(PaymentStatus.PENDING)).thenReturn(5L);
        when(classSessionRepository.countByStatusAndScheduledDateBetween(
                eq(ClassStatus.SCHEDULED),
                any(LocalDateTime.class),
                any(LocalDateTime.class)))
                .thenReturn(20L);
        when(classSessionRepository.countByStatusAndScheduledDateBetween(
                eq(ClassStatus.COMPLETED),
                any(LocalDateTime.class),
                any(LocalDateTime.class)))
                .thenReturn(35L);
        when(attendanceRepository.calculateOverallAttendanceRate()).thenReturn(85.5);
        when(classSessionRepository.findUpcomingSessionsByDateRange(
                any(LocalDateTime.class),
                any(LocalDateTime.class)))
                .thenReturn(new ArrayList<>());
        when(attendanceRepository.findStudentsWithLowAttendance(50.0))
                .thenReturn(new ArrayList<>());

        // When
        DashboardSummaryDTO result = dashboardService.getDashboardSummary();

        // Then
        assertNotNull(result);
        assertEquals(100L, result.getTotalStudents());
        assertEquals(85L, result.getActiveStudents());
        assertEquals(10L, result.getNewStudentsThisMonth());
        assertEquals(15L, result.getTotalCourses());
        assertEquals(12L, result.getActiveCourses());
        assertEquals(200L, result.getTotalEnrollments());
        assertEquals(180L, result.getActiveEnrollments());
        assertEquals(new BigDecimal("50000.00"), result.getMonthlyRevenue());
        assertEquals(45L, result.getPaymentsThisMonth());
        assertEquals(new BigDecimal("5000.00"), result.getPendingPayments());
        assertEquals(5L, result.getPendingPaymentsCount());
        assertEquals(20L, result.getUpcomingSessionsThisWeek());
        assertEquals(35L, result.getCompletedSessionsThisMonth());
        assertEquals(85.5, result.getAverageAttendanceRate());
        assertEquals(0L, result.getStudentsWithLowAttendance());

        // Verify
        verify(studentRepository, times(1)).count();
        verify(studentRepository, times(1)).countByStatus(StudentStatus.ACTIVE);
        verify(courseRepository, times(1)).count();
        verify(courseRepository, times(1)).countByIsActiveTrue();
    }

    @Test
    void testGetDashboardSummary_WithNullValues() {
        // Given - Simular valores nulos de la base de datos
        when(studentRepository.count()).thenReturn(100L);
        when(studentRepository.countByStatus(StudentStatus.ACTIVE)).thenReturn(85L);
        when(courseRepository.count()).thenReturn(15L);
        when(courseRepository.countByIsActiveTrue()).thenReturn(12L);
        when(enrollmentRepository.count()).thenReturn(200L);
        when(enrollmentRepository.countByStatus(EnrollmentStatus.ACTIVE)).thenReturn(180L);
        when(studentRepository.countByCreatedAtBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(0L);
        when(paymentRepository.sumAmountByStatusAndDateBetween(
                eq(PaymentStatus.COMPLETED),
                any(LocalDateTime.class),
                any(LocalDateTime.class)))
                .thenReturn(null); // Simular null
        when(paymentRepository.countByStatusAndPaymentDateBetween(
                eq(PaymentStatus.COMPLETED),
                any(LocalDateTime.class),
                any(LocalDateTime.class)))
                .thenReturn(0L);
        when(paymentRepository.sumAmountByStatus(PaymentStatus.PENDING))
                .thenReturn(null); // Simular null
        when(paymentRepository.countByStatus(PaymentStatus.PENDING)).thenReturn(0L);
        when(classSessionRepository.countByStatusAndScheduledDateBetween(
                any(ClassStatus.class),
                any(LocalDateTime.class),
                any(LocalDateTime.class)))
                .thenReturn(0L);
        when(attendanceRepository.calculateOverallAttendanceRate()).thenReturn(null);
        when(classSessionRepository.findUpcomingSessionsByDateRange(
                any(LocalDateTime.class),
                any(LocalDateTime.class)))
                .thenReturn(new ArrayList<>());
        when(attendanceRepository.findStudentsWithLowAttendance(50.0))
                .thenReturn(new ArrayList<>());

        // When
        DashboardSummaryDTO result = dashboardService.getDashboardSummary();

        // Then
        assertNotNull(result);
        assertEquals(BigDecimal.ZERO, result.getMonthlyRevenue());
        assertEquals(BigDecimal.ZERO, result.getPendingPayments());
        assertEquals(0.0, result.getAverageAttendanceRate());
    }

    @Test
    void testGetQuickStats() {
        // Given
        when(studentRepository.countByStatus(StudentStatus.ACTIVE)).thenReturn(85L);
        when(courseRepository.countByIsActiveTrue()).thenReturn(12L);
        when(enrollmentRepository.countByStatus(EnrollmentStatus.ACTIVE)).thenReturn(180L);
        when(classSessionRepository.countByStatusAndScheduledDateBetween(
                eq(ClassStatus.SCHEDULED),
                any(LocalDateTime.class),
                any(LocalDateTime.class)))
                .thenReturn(15L);

        // When
        DashboardSummaryDTO result = dashboardService.getQuickStats();

        // Then
        assertNotNull(result);
        assertEquals(85L, result.getActiveStudents());
        assertEquals(12L, result.getActiveCourses());
        assertEquals(180L, result.getActiveEnrollments());
        assertEquals(15L, result.getUpcomingSessionsThisWeek());

        // Verify que solo se llamen los métodos necesarios
        verify(studentRepository, times(1)).countByStatus(StudentStatus.ACTIVE);
        verify(courseRepository, times(1)).countByIsActiveTrue();
        verify(enrollmentRepository, times(1)).countByStatus(EnrollmentStatus.ACTIVE);
        verify(classSessionRepository, times(1)).countByStatusAndScheduledDateBetween(
                eq(ClassStatus.SCHEDULED),
                any(LocalDateTime.class),
                any(LocalDateTime.class));

        // Verificar que NO se llamen métodos de reportes financieros
        verify(paymentRepository, never()).sumAmountByStatusAndDateBetween(
                any(PaymentStatus.class),
                any(LocalDateTime.class),
                any(LocalDateTime.class));
    }
}
