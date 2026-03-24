package com.edidev.academyApp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendanceDTO {

    private Long id;

    // Estudiante
    private Long studentId;
    private String studentName;
    private String studentEmail;

    // Sesión de clase
    private Long classSessionId;
    private String sessionName;
    private LocalDateTime scheduledDate;
    private Long courseId;
    private String courseName;

    // Datos de asistencia
    private Boolean attended;
    private Boolean isLate;
    private Boolean isExcused;

    private LocalDateTime attendanceDate;
    private LocalDateTime arrivalTime;
    private LocalDateTime departureTime;

    private String notes;
    private String recordedBy;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
