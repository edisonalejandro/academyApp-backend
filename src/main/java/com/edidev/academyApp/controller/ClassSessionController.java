package com.edidev.academyApp.controller;

import com.edidev.academyApp.dto.ClassSessionDTO;
import com.edidev.academyApp.service.ClassSessionService;
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
@RequestMapping("/api/class-sessions")
@RequiredArgsConstructor
@Tag(name = "Class Sessions", description = "APIs para gestión de sesiones de clase")
public class ClassSessionController {

    private final ClassSessionService classSessionService;

    // =========================================================
    // CONSULTAS — accesibles por todos los autenticados
    // =========================================================

    @GetMapping("/course/{courseId}")
    @Operation(summary = "Obtener todas las sesiones de un curso")
    public ResponseEntity<List<ClassSessionDTO>> getSessionsByCourse(@PathVariable Long courseId) {
        return ResponseEntity.ok(classSessionService.getSessionsByCourse(courseId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener sesión por ID")
    public ResponseEntity<ClassSessionDTO> getSessionById(@PathVariable Long id) {
        return ResponseEntity.ok(classSessionService.getSessionById(id));
    }

    @GetMapping("/upcoming/{courseId}")
    @Operation(summary = "Obtener próximas sesiones programadas de un curso")
    public ResponseEntity<List<ClassSessionDTO>> getUpcomingSessions(@PathVariable Long courseId) {
        return ResponseEntity.ok(classSessionService.getUpcomingSessions(courseId));
    }

    @GetMapping("/calendar")
    @Operation(summary = "Obtener sesiones en un rango de fechas (calendario)")
    public ResponseEntity<List<ClassSessionDTO>> getSessionsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        return ResponseEntity.ok(classSessionService.getSessionsByDateRange(from, to));
    }

    // =========================================================
    // CONSULTAS — solo ADMIN / TEACHER
    // =========================================================

    @GetMapping("/teacher/{teacherId}")
    @Operation(summary = "Obtener sesiones de un profesor")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<List<ClassSessionDTO>> getSessionsByTeacher(@PathVariable Long teacherId) {
        return ResponseEntity.ok(classSessionService.getSessionsByTeacher(teacherId));
    }

    @GetMapping
    @Operation(summary = "Obtener todas las sesiones")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<List<ClassSessionDTO>> getAllSessions() {
        return ResponseEntity.ok(classSessionService.getAllSessions());
    }

    // =========================================================
    // GESTIÓN — solo ADMIN / TEACHER
    // =========================================================

    @PostMapping
    @Operation(summary = "Crear una nueva sesión de clase")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<ClassSessionDTO> createSession(@RequestBody ClassSessionDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(classSessionService.createSession(dto));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar datos de una sesión")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<ClassSessionDTO> updateSession(@PathVariable Long id, @RequestBody ClassSessionDTO dto) {
        return ResponseEntity.ok(classSessionService.updateSession(id, dto));
    }

    @PatchMapping("/{id}/cancel")
    @Operation(summary = "Cancelar una sesión")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<ClassSessionDTO> cancelSession(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body) {
        String reason = body != null ? body.get("reason") : null;
        return ResponseEntity.ok(classSessionService.cancelSession(id, reason));
    }

    @PatchMapping("/{id}/start")
    @Operation(summary = "Marcar sesión como iniciada")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<ClassSessionDTO> startSession(@PathVariable Long id) {
        return ResponseEntity.ok(classSessionService.startSession(id));
    }

    @PatchMapping("/{id}/complete")
    @Operation(summary = "Marcar sesión como completada")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<ClassSessionDTO> completeSession(@PathVariable Long id) {
        return ResponseEntity.ok(classSessionService.completeSession(id));
    }
}
