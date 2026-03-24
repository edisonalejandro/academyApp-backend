package com.edidev.academyApp.controller;

import com.edidev.academyApp.dto.StudentDTO;
import com.edidev.academyApp.enums.StudentCategory;
import com.edidev.academyApp.enums.StudentStatus;
import com.edidev.academyApp.service.StudentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
@Tag(name = "Students", description = "APIs para gestión de estudiantes")
public class StudentController {

    private final StudentService studentService;

    @GetMapping
    @Operation(summary = "Obtener todos los estudiantes")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<List<StudentDTO>> getAllStudents() {
        return ResponseEntity.ok(studentService.getAllStudents());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener estudiante por ID")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<StudentDTO> getStudentById(@PathVariable Long id) {
        return ResponseEntity.ok(studentService.getStudentById(id));
    }

    @GetMapping("/me")
    @Operation(summary = "Obtener el perfil del estudiante autenticado")
    public ResponseEntity<StudentDTO> getMyProfile(Authentication authentication) {
        StudentDTO student = studentService.getStudentByEmail(authentication.getName());
        return ResponseEntity.ok(student);
    }

    @GetMapping("/search")
    @Operation(summary = "Buscar estudiantes por nombre, email o teléfono")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<List<StudentDTO>> searchStudents(
            @RequestParam(required = false) String q) {
        return ResponseEntity.ok(studentService.searchStudents(q));
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Obtener estudiantes por estado")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<List<StudentDTO>> getStudentsByStatus(
            @PathVariable StudentStatus status) {
        return ResponseEntity.ok(studentService.getStudentsByStatus(status));
    }

    @GetMapping("/category/{category}")
    @Operation(summary = "Obtener estudiantes por categoría")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<List<StudentDTO>> getStudentsByCategory(
            @PathVariable StudentCategory category) {
        return ResponseEntity.ok(studentService.getStudentsByCategory(category));
    }

    @PostMapping
    @Operation(summary = "Crear nuevo estudiante")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<StudentDTO> createStudent(@Valid @RequestBody StudentDTO studentDTO) {
        StudentDTO created = studentService.createStudent(studentDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar datos del estudiante")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<StudentDTO> updateStudent(
            @PathVariable Long id,
            @Valid @RequestBody StudentDTO studentDTO) {
        return ResponseEntity.ok(studentService.updateStudent(id, studentDTO));
    }

    @PatchMapping("/{id}/category")
    @Operation(summary = "Cambiar categoría del estudiante")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<StudentDTO> updateCategory(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        StudentCategory category = StudentCategory.valueOf(body.get("category"));
        return ResponseEntity.ok(studentService.updateCategory(id, category));
    }
}
