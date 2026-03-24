package com.edidev.academyApp.model;

import com.edidev.academyApp.enums.DanceType;
import com.edidev.academyApp.enums.DanceLevel;
import com.edidev.academyApp.enums.EnrollmentStatus;
import com.edidev.academyApp.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "courses")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(exclude = {"enrollments", "classSessions", "payments", "teacher"})
@ToString(exclude = {"enrollments", "classSessions", "payments", "teacher"})
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title", nullable = false, length = 100)
    private String title;

    @Column(name = "code", unique = true, nullable = false, length = 20)
    private String code;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "dance_type", nullable = false)
    private DanceType danceType;

    @Enumerated(EnumType.STRING)
    @Column(name = "level", nullable = false)
    private DanceLevel level;

    // Precio base por hora (puede ser sobrescrito por las reglas de precios)
    @Column(name = "price_per_hour", precision = 10, scale = 2)
    private BigDecimal pricePerHour;

    // Duración estándar de cada clase en horas
    @Column(name = "duration_hours", precision = 3, scale = 1)
    @Builder.Default
    private BigDecimal durationHours = BigDecimal.valueOf(1.5);

    // Capacidad máxima del curso
    @Column(name = "max_capacity")
    @Builder.Default
    private Integer maxCapacity = 20;

    // Instructor asignado al curso
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", nullable = false)
    private User teacher;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    // Imagen del curso (URL)
    @Column(name = "image_url")
    private String imageUrl;

    // Requisitos previos
    @Column(name = "prerequisites")
    private String prerequisites;

    // Objetivos del curso
    @Column(name = "objectives", columnDefinition = "TEXT")
    private String objectives;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Relaciones
    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Enrollment> enrollments = new ArrayList<>();

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<ClassSession> classSessions = new ArrayList<>();

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Payment> payments = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (code == null || code.isEmpty()) {
            code = generateCourseCode();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Genera un código único para el curso
     */
    private String generateCourseCode() {
        String dancePrefix = danceType.name().substring(0, 3).toUpperCase();
        String levelPrefix = level.name().substring(0, 1).toUpperCase();
        return dancePrefix + "-" + levelPrefix + "-" + System.currentTimeMillis() % 10000;
    }

    /**
     * Obtiene el número de estudiantes inscritos activos
     */
    public int getActiveEnrollmentsCount() {
        return (int) enrollments.stream()
                .filter(Enrollment::isActive)
                .count();
    }

    /**
     * Verifica si hay cupos disponibles
     */
    public boolean hasAvailableSlots() {
        Integer maxCapacity = getMaxCapacity();
        if (maxCapacity == null) {
            return true;
        }
        return getActiveEnrollmentsCount() < maxCapacity;
    }

    /**
     * Obtiene los cupos disponibles
     */
    public int getAvailableSlots() {
        return Math.max(0, maxCapacity - getActiveEnrollmentsCount());
    }

    /**
     * Verifica si un usuario puede inscribirse en este curso
     */
    public boolean canUserEnroll(User user) {
        // Verificar si ya está inscrito activamente
        boolean alreadyEnrolled = enrollments.stream()
                .anyMatch(enrollment -> 
                    enrollment.getStudent().getId().equals(user.getId()) && 
                    enrollment.getStatus() == EnrollmentStatus.ACTIVE
                );
        
        return !alreadyEnrolled && hasAvailableSlots() && isActive;
    }

    /**
     * Obtiene el total de ingresos del curso
     */
    public BigDecimal getTotalRevenue() {
        return payments.stream()
                .filter(payment -> payment.getStatus() == PaymentStatus.COMPLETED)
                .map(Payment::getFinalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Obtiene el nombre completo del curso para mostrar
     */
    public String getFullDisplayName() {
        return danceType.getDisplayName() + " - " + level.getDisplayName() + " (" + code + ")";
    }

    /**
     * Verifica si el usuario es el instructor del curso
     */
    public boolean isInstructor(User user) {
        return teacher != null && teacher.equals(user);
    }
}