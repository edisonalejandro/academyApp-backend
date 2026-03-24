package com.edidev.academyApp.controller;

import com.edidev.academyApp.dto.EnrollmentDTO;
import com.edidev.academyApp.dto.EnrollmentSummaryDTO;
import com.edidev.academyApp.service.EnrollmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/enrollments")
@RequiredArgsConstructor
@Tag(name = "Enrollments", description = "APIs para consulta de inscripciones")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    @GetMapping("/my")
    @Operation(summary = "Obtener las inscripciones del usuario autenticado")
    public ResponseEntity<List<EnrollmentDTO>> getMyEnrollments(Authentication authentication) {
        List<EnrollmentDTO> enrollments = enrollmentService.getEnrollmentsByUser(authentication.getName());
        return ResponseEntity.ok(enrollments);
    }

    @GetMapping("/my/active")
    @Operation(summary = "Obtener inscripciones activas del usuario autenticado")
    public ResponseEntity<List<EnrollmentDTO>> getMyActiveEnrollments(Authentication authentication) {
        List<EnrollmentDTO> enrollments = enrollmentService.getActiveEnrollmentsByUser(authentication.getName());
        return ResponseEntity.ok(enrollments);
    }

    @GetMapping("/my/summary")
    @Operation(summary = "Obtener resumen de horas e inscripciones del usuario autenticado")
    public ResponseEntity<EnrollmentSummaryDTO> getMyEnrollmentSummary(Authentication authentication) {
        EnrollmentSummaryDTO summary = enrollmentService.getEnrollmentSummary(authentication.getName());
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener inscripción por ID")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<EnrollmentDTO> getEnrollmentById(@PathVariable Long id) {
        return ResponseEntity.ok(enrollmentService.getEnrollmentById(id));
    }

    @GetMapping("/course/{courseId}")
    @Operation(summary = "Obtener todas las inscripciones de un curso")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<List<EnrollmentDTO>> getEnrollmentsByCourse(@PathVariable Long courseId) {
        return ResponseEntity.ok(enrollmentService.getEnrollmentsByCourse(courseId));
    }

    @PatchMapping("/{id}/cancel")
    @Operation(summary = "Cancelar una inscripción")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EnrollmentDTO> cancelEnrollment(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body) {
        String reason = body != null ? body.getOrDefault("reason", "") : "";
        return ResponseEntity.ok(enrollmentService.cancelEnrollment(id, reason));
    }

    @PatchMapping("/{id}/suspend")
    @Operation(summary = "Suspender una inscripción")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EnrollmentDTO> suspendEnrollment(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body) {
        String reason = body != null ? body.getOrDefault("reason", "") : "";
        return ResponseEntity.ok(enrollmentService.suspendEnrollment(id, reason));
    }

    @PatchMapping("/{id}/reactivate")
    @Operation(summary = "Reactivar una inscripción suspendida")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EnrollmentDTO> reactivateEnrollment(@PathVariable Long id) {
        return ResponseEntity.ok(enrollmentService.reactivateEnrollment(id));
    }
}
