package com.edidev.academyApp.controller;

import com.edidev.academyApp.dto.AttendanceDTO;
import com.edidev.academyApp.dto.AttendanceReportDTO;
import com.edidev.academyApp.service.impl.AttendanceServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/attendances")
@RequiredArgsConstructor
@Tag(name = "Attendances", description = "APIs para gestión de asistencias")
public class AttendanceController {

    private final AttendanceServiceImpl attendanceService;

    // =========================================================
    // REGISTRO / MODIFICACIÓN
    // =========================================================

    @PostMapping
    @Operation(summary = "Registrar asistencia de un estudiante a una sesión")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<AttendanceDTO> recordAttendance(@RequestBody Map<String, Object> body) {
        Long studentId = toLong(body.get("studentId"));
        Long classId = toLong(body.get("classSessionId"));
        Boolean attended = (Boolean) body.get("attended");
        String notes = (String) body.get("notes");

        AttendanceDTO dto = attendanceService.recordAttendanceDTO(studentId, classId, attended, notes);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar estado de asistencia")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<AttendanceDTO> updateAttendance(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body) {
        Boolean attended = (Boolean) body.get("attended");
        String notes = (String) body.get("notes");
        return ResponseEntity.ok(attendanceService.updateAttendanceDTO(id, attended, notes));
    }

    // =========================================================
    // CONSULTAS POR SESIÓN
    // =========================================================

    @GetMapping("/session/{sessionId}")
    @Operation(summary = "Obtener todas las asistencias de una sesión de clase")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<List<AttendanceDTO>> getSessionAttendances(@PathVariable Long sessionId) {
        return ResponseEntity.ok(attendanceService.getClassAttendanceDTOs(sessionId));
    }

    // =========================================================
    // CONSULTAS POR ESTUDIANTE
    // =========================================================

    @GetMapping("/student/{studentId}")
    @Operation(summary = "Obtener historial de asistencias de un estudiante")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<List<AttendanceDTO>> getStudentAttendances(@PathVariable Long studentId) {
        return ResponseEntity.ok(attendanceService.getStudentAttendanceDTOs(studentId));
    }

    @GetMapping("/student/{studentId}/rate")
    @Operation(summary = "Obtener porcentaje de asistencia de un estudiante")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<Map<String, Object>> getStudentAttendanceRate(@PathVariable Long studentId) {
        Double rate = attendanceService.calculateStudentAttendanceRate(studentId);
        return ResponseEntity.ok(Map.of(
                "studentId", studentId,
                "attendanceRate", rate
        ));
    }

    @GetMapping("/student/{studentId}/check/{classSessionId}")
    @Operation(summary = "Verificar si un estudiante tiene registro en una sesión")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<Map<String, Object>> hasAttendanceRecord(
            @PathVariable Long studentId,
            @PathVariable Long classSessionId) {
        Boolean has = attendanceService.hasAttendanceRecord(studentId, classSessionId);
        return ResponseEntity.ok(Map.of("hasRecord", has));
    }

    // =========================================================
    // REPORTES — solo ADMIN
    // =========================================================

    @GetMapping("/report")
    @Operation(summary = "Generar reporte de asistencias en un rango de fechas")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AttendanceReportDTO> generateReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        return ResponseEntity.ok(attendanceService.generateAttendanceReport(from, to));
    }

    @GetMapping("/low-attendance")
    @Operation(summary = "Obtener estudiantes con baja asistencia")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<List<Map<String, Object>>> getLowAttendanceStudents(
            @RequestParam(defaultValue = "75.0") Double minPercentage) {
        return ResponseEntity.ok(attendanceService.getStudentsWithLowAttendance(minPercentage));
    }

    // =========================================================
    // HELPER
    // =========================================================

    private Long toLong(Object val) {
        if (val == null) return null;
        if (val instanceof Integer) return ((Integer) val).longValue();
        if (val instanceof Long) return (Long) val;
        if (val instanceof Number) return ((Number) val).longValue();
        return Long.parseLong(val.toString());
    }
}
