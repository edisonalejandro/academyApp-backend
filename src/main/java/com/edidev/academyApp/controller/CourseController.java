package com.edidev.academyApp.controller;

import com.edidev.academyApp.dto.CourseDTO;
import com.edidev.academyApp.enums.DanceLevel;
import com.edidev.academyApp.enums.DanceType;
import com.edidev.academyApp.service.CourseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
@Tag(name = "Courses", description = "APIs para gestión de cursos de baile")
public class CourseController {

    private final CourseService courseService;

    @GetMapping
    @Operation(summary = "Obtener todos los cursos activos")
    public ResponseEntity<List<CourseDTO>> getAllCourses() {
        return ResponseEntity.ok(courseService.getAllActiveCourses());
    }

    @GetMapping("/all")
    @Operation(summary = "Obtener todos los cursos (incluye inactivos)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<CourseDTO>> getAllCoursesAdmin() {
        return ResponseEntity.ok(courseService.getAllCourses());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener curso por ID")
    public ResponseEntity<CourseDTO> getCourseById(@PathVariable Long id) {
        return ResponseEntity.ok(courseService.getCourseById(id));
    }

    @GetMapping("/search")
    @Operation(summary = "Buscar cursos por texto (título, descripción, código)")
    public ResponseEntity<List<CourseDTO>> searchCourses(
            @RequestParam(required = false) String q) {
        return ResponseEntity.ok(courseService.searchCourses(q));
    }

    @GetMapping("/dance-type/{danceType}")
    @Operation(summary = "Obtener cursos por tipo de baile")
    public ResponseEntity<List<CourseDTO>> getCoursesByDanceType(
            @PathVariable DanceType danceType) {
        return ResponseEntity.ok(courseService.getCoursesByDanceType(danceType));
    }

    @GetMapping("/level/{level}")
    @Operation(summary = "Obtener cursos por nivel")
    public ResponseEntity<List<CourseDTO>> getCoursesByLevel(
            @PathVariable DanceLevel level) {
        return ResponseEntity.ok(courseService.getCoursesByLevel(level));
    }

    @GetMapping("/teacher/{teacherId}")
    @Operation(summary = "Obtener cursos de un profesor")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<List<CourseDTO>> getCoursesByTeacher(
            @PathVariable Long teacherId) {
        return ResponseEntity.ok(courseService.getCoursesByTeacher(teacherId));
    }

    @GetMapping("/{id}/capacity")
    @Operation(summary = "Obtener cupos disponibles de un curso")
    public ResponseEntity<Map<String, Object>> getCourseCapacity(@PathVariable Long id) {
        CourseDTO course = courseService.getCourseById(id);
        long available = courseService.getAvailableCapacity(id);
        return ResponseEntity.ok(Map.of(
                "courseId", id,
                "courseTitle", course.getTitle(),
                "maxCapacity", course.getMaxCapacity(),
                "activeEnrollments", course.getActiveEnrollments(),
                "availableSlots", available
        ));
    }

    @PostMapping
    @Operation(summary = "Crear nuevo curso")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CourseDTO> createCourse(@Valid @RequestBody CourseDTO courseDTO) {
        CourseDTO created = courseService.createCourse(courseDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar curso completo")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CourseDTO> updateCourse(
            @PathVariable Long id,
            @Valid @RequestBody CourseDTO courseDTO) {
        return ResponseEntity.ok(courseService.updateCourse(id, courseDTO));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar curso (eliminación lógica)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCourse(@PathVariable Long id) {
        courseService.deleteCourse(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/toggle-status")
    @Operation(summary = "Activar o desactivar curso")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CourseDTO> toggleCourseStatus(@PathVariable Long id) {
        return ResponseEntity.ok(courseService.toggleCourseStatus(id));
    }
}
