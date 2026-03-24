package com.edidev.academyApp.service;

import com.edidev.academyApp.dto.EnrollmentDTO;
import com.edidev.academyApp.dto.EnrollmentSummaryDTO;
import com.edidev.academyApp.enums.EnrollmentStatus;
import com.edidev.academyApp.exception.EnrollmentNotFoundException;
import com.edidev.academyApp.exception.UserNotFoundException;
import com.edidev.academyApp.model.*;
import com.edidev.academyApp.repository.EnrollmentRepository;
import com.edidev.academyApp.repository.StudentRepository;
import com.edidev.academyApp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final UserRepository userRepository;
    private final StudentRepository studentRepository;

    /**
     * Crea o actualiza una inscripción basada en un pago
     */
    public Enrollment createOrUpdateEnrollment(User user, Course course, Payment payment) {
        log.info("Creando/actualizando inscripción para usuario: {} en curso: {}", 
                user.getEmail(), course.getTitle());

        // Buscar el Student asociado al User
        Student student = studentRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Estudiante no encontrado para el usuario: " + user.getEmail()));

        // Buscar inscripción existente
        Optional<Enrollment> existingEnrollment = enrollmentRepository
                .findByUserIdAndCourseIdAndStatus(user.getId(), course.getId(), 
                        EnrollmentStatus.ACTIVE);

        Enrollment enrollment;
        
        if (existingEnrollment.isPresent()) {
            // Actualizar inscripción existente
            enrollment = existingEnrollment.get();
            log.info("Actualizando inscripción existente ID: {}", enrollment.getId());
            
            // Agregar las horas compradas
            BigDecimal newHours = BigDecimal.valueOf(payment.getQuantityClasses())
                    .multiply(course.getDurationHours());
            enrollment.setPurchasedHours(enrollment.getPurchasedHours().add(newHours));
            
            // Actualizar precio total pagado
            enrollment.setTotalPaid(enrollment.getTotalPaid().add(payment.getFinalPrice()));
            
        } else {
            // Crear nueva inscripción
            log.info("Creando nueva inscripción");
            
            BigDecimal totalHours = BigDecimal.valueOf(payment.getQuantityClasses())
                    .multiply(course.getDurationHours());
            
            enrollment = Enrollment.builder()
                    .student(student)
                    .course(course)
                    .purchasedHours(totalHours)
                    .usedHours(BigDecimal.ZERO)
                    .totalPaid(payment.getFinalPrice())
                    .status(EnrollmentStatus.ACTIVE)
                    .build();
        }

        // Actualizar timestamp
        enrollment.setUpdatedAt(LocalDateTime.now());
        
        Enrollment savedEnrollment = enrollmentRepository.save(enrollment);
        log.info("Inscripción guardada exitosamente - ID: {}, Horas totales: {}", 
                savedEnrollment.getId(), savedEnrollment.getPurchasedHours());
        
        return savedEnrollment;
    }

    /**
     * Obtiene las inscripciones de un usuario
     */
    public List<EnrollmentDTO> getEnrollmentsByUser(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado: " + userEmail));

        return enrollmentRepository.findByUserIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(this::mapToEnrollmentDTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene las inscripciones activas de un usuario
     */
    public List<EnrollmentDTO> getActiveEnrollmentsByUser(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado: " + userEmail));

        return enrollmentRepository.findByUserIdAndStatus(user.getId(), EnrollmentStatus.ACTIVE)
                .stream()
                .map(this::mapToEnrollmentDTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene las inscripciones de un curso
     */
    public List<EnrollmentDTO> getEnrollmentsByCourse(Long courseId) {
        return enrollmentRepository.findByCourseIdOrderByCreatedAtDesc(courseId)
                .stream()
                .map(this::mapToEnrollmentDTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene una inscripción específica
     */
    public EnrollmentDTO getEnrollmentById(Long enrollmentId) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new EnrollmentNotFoundException("Inscripción no encontrada: " + enrollmentId));
        
        return mapToEnrollmentDTO(enrollment);
    }

    /**
     * Consume horas de una inscripción (cuando el estudiante asiste a clase)
     */
    public Enrollment consumeHours(Long enrollmentId, BigDecimal hoursToConsume) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new EnrollmentNotFoundException("Inscripción no encontrada: " + enrollmentId));

        BigDecimal remainingHours = enrollment.getRemainingHours();
        
        if (remainingHours.compareTo(hoursToConsume) < 0) {
            throw new RuntimeException("No hay suficientes horas disponibles. " +
                    "Disponibles: " + remainingHours + ", Solicitadas: " + hoursToConsume);
        }

        enrollment.setUsedHours(enrollment.getUsedHours().add(hoursToConsume));
        
        // Si se agotaron las horas, cambiar estado
        if (enrollment.getRemainingHours().compareTo(BigDecimal.ZERO) <= 0) {
            enrollment.setStatus(EnrollmentStatus.HOURS_EXHAUSTED);
            log.info("Horas agotadas para inscripción ID: {}", enrollmentId);
        }

        Enrollment savedEnrollment = enrollmentRepository.save(enrollment);
        log.info("Consumidas {} horas de inscripción ID: {}. Restantes: {}", 
                hoursToConsume, enrollmentId, savedEnrollment.getRemainingHours());
        
        return savedEnrollment;
    }

    /**
     * Suspende una inscripción
     */
    public EnrollmentDTO suspendEnrollment(Long enrollmentId, String reason) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new EnrollmentNotFoundException("Inscripción no encontrada: " + enrollmentId));

        enrollment.setStatus(EnrollmentStatus.SUSPENDED);
        enrollment.setNotes(enrollment.getNotes() + "\nSuspendido: " + reason);
        
        Enrollment savedEnrollment = enrollmentRepository.save(enrollment);
        log.info("Inscripción suspendida ID: {}, Razón: {}", enrollmentId, reason);
        
        return mapToEnrollmentDTO(savedEnrollment);
    }

    /**
     * Reactiva una inscripción suspendida
     */
    public EnrollmentDTO reactivateEnrollment(Long enrollmentId) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new EnrollmentNotFoundException("Inscripción no encontrada: " + enrollmentId));

        if (enrollment.getRemainingHours().compareTo(BigDecimal.ZERO) > 0) {
            enrollment.setStatus(EnrollmentStatus.ACTIVE);
        } else {
            enrollment.setStatus(EnrollmentStatus.HOURS_EXHAUSTED);
        }
        
        enrollment.setNotes(enrollment.getNotes() + "\nReactivado: " + LocalDateTime.now());
        
        Enrollment savedEnrollment = enrollmentRepository.save(enrollment);
        log.info("Inscripción reactivada ID: {}", enrollmentId);
        
        return mapToEnrollmentDTO(savedEnrollment);
    }

    /**
     * Cancela una inscripción
     */
    public EnrollmentDTO cancelEnrollment(Long enrollmentId, String reason) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new EnrollmentNotFoundException("Inscripción no encontrada: " + enrollmentId));

        enrollment.setStatus(EnrollmentStatus.CANCELLED);
        enrollment.setNotes(enrollment.getNotes() + "\nCancelado: " + reason);
        
        Enrollment savedEnrollment = enrollmentRepository.save(enrollment);
        log.info("Inscripción cancelada ID: {}, Razón: {}", enrollmentId, reason);
        
        return mapToEnrollmentDTO(savedEnrollment);
    }

    /**
     * Verifica si un usuario puede inscribirse en un curso
     */
    public boolean canUserEnrollInCourse(Long userId, Long courseId) {
        // Verificar si ya tiene una inscripción activa
        boolean hasActiveEnrollment = enrollmentRepository
                .existsByUserIdAndCourseIdAndStatus(userId, courseId, EnrollmentStatus.ACTIVE);
        
        return !hasActiveEnrollment;
    }

    /**
     * Obtiene el resumen de inscripciones de un usuario
     */
    public EnrollmentSummaryDTO getEnrollmentSummary(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado: " + userEmail));

        List<Enrollment> enrollments = enrollmentRepository.findByUserId(user.getId());
        
        long activeCount = enrollments.stream()
                .filter(e -> e.getStatus() == EnrollmentStatus.ACTIVE)
                .count();
        
        BigDecimal totalHoursPurchased = enrollments.stream()
                .map(Enrollment::getPurchasedHours)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal totalHoursUsed = enrollments.stream()
                .map(Enrollment::getUsedHours)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal totalPaid = enrollments.stream()
                .map(Enrollment::getTotalPaid)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return EnrollmentSummaryDTO.builder()
                .totalEnrollments(enrollments.size())
                .activeEnrollments((int) activeCount)
                .totalHoursPurchased(totalHoursPurchased)
                .totalHoursUsed(totalHoursUsed)
                .totalHoursRemaining(totalHoursPurchased.subtract(totalHoursUsed))
                .totalAmountPaid(totalPaid)
                .build();
    }

    // MÉTODOS PRIVADOS

    private EnrollmentDTO mapToEnrollmentDTO(Enrollment enrollment) {
        User user = enrollment.getUser();
        return EnrollmentDTO.builder()
                .id(enrollment.getId())
                .studentId(enrollment.getStudent() != null ? enrollment.getStudent().getId() : null)
                .studentName(user != null ? (user.getFirstName() + " " + user.getLastName()) : "N/A")
                .courseId(enrollment.getCourse().getId())
                .courseName(enrollment.getCourse().getTitle())
                .courseCode(enrollment.getCourse().getCode())
                .purchasedHours(enrollment.getPurchasedHours())
                .usedHours(enrollment.getUsedHours())
                .remainingHours(enrollment.getRemainingHours())
                .totalPaid(enrollment.getTotalPaid())
                .status(enrollment.getStatus())
                .enrollmentDate(enrollment.getCreatedAt())
                .updatedAt(enrollment.getUpdatedAt())
                .notes(enrollment.getNotes())
                .build();
    }


}