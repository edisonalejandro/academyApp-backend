package com.edidev.academyApp.service;

import com.edidev.academyApp.dto.AttendanceReportDTO;
import com.edidev.academyApp.model.Attendance;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface AttendanceService {
    
    /**
     * Registrar asistencia
     */
    Attendance recordAttendance(Long studentId, Long classId, Boolean attended, String notes);
    
    /**
     * Obtener asistencias de un estudiante
     */
    List<Attendance> getStudentAttendances(Long studentId);
    
    /**
     * Obtener asistencias de una clase
     */
    List<Attendance> getClassAttendances(Long classId);
    
    /**
     * Calcular porcentaje de asistencia de un estudiante
     */
    Double calculateStudentAttendanceRate(Long studentId);
    
    /**
     * Generar reporte de asistencia
     */
    AttendanceReportDTO generateAttendanceReport(LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Obtener estudiantes con baja asistencia
     */
    List<Map<String, Object>> getStudentsWithLowAttendance(Double minPercentage);
    
    /**
     * Actualizar asistencia existente
     */
    Attendance updateAttendance(Long attendanceId, Boolean attended, String notes);
    
    /**
     * Validar si existe registro de asistencia
     */
    Boolean hasAttendanceRecord(Long studentId, Long classId);
}