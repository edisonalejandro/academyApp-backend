package com.edidev.academyApp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendanceReportDTO {

    private LocalDateTime reportDate;
    private LocalDateTime periodStart;
    private LocalDateTime periodEnd;
    
    // Estadísticas generales
    private Long totalClasses;
    private Long totalAttendances;
    private Long totalAbsences;
    private Double overallAttendanceRate;
    
    // Reportes detallados
    private List<CourseAttendanceStats> courseStats;
    private List<StudentAttendanceStats> studentStats;
    private List<Map<String, Object>> dayOfWeekStats;
    private List<Map<String, Object>> monthlyTrend;
    
    // Alertas y recomendaciones
    private List<StudentAttendanceStats> lowAttendanceStudents;
    private List<StudentAttendanceStats> topAttendanceStudents;
    private List<String> recommendations;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CourseAttendanceStats {
        private Long courseId;
        private String courseName;
        private Long totalSessions;
        private Long totalAttendances;
        private Long totalAbsences;
        private Double attendanceRate;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class StudentAttendanceStats {
        private Long studentId;
        private String firstName;
        private String lastName;
        private String email;
        private Long totalClasses;
        private Long classesAttended;
        private Long classesMissed;
        private Double attendanceRate;
        private String riskLevel; // LOW, MEDIUM, HIGH
    }
}