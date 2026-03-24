package com.edidev.academyApp.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "attendances", indexes = {
    @Index(name = "idx_attendance_student_id", columnList = "student_id"),
    @Index(name = "idx_attendance_class_id", columnList = "class_session_id"),
    @Index(name = "idx_attendance_date", columnList = "attendance_date"),
    @Index(name = "idx_attendance_student_class", columnList = "student_id, class_session_id", unique = true)
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(exclude = {"student", "classSession"})
@ToString(exclude = {"student", "classSession"})
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_session_id", nullable = false) // ✅ Nombre correcto
    private ClassSession classSession;

    @Column(name = "attended", nullable = false)
    @Builder.Default
    private Boolean attended = false;

    @Column(name = "attendance_date", nullable = false)
    private LocalDateTime attendanceDate;

    @Column(name = "arrival_time")
    private LocalDateTime arrivalTime;

    @Column(name = "departure_time")
    private LocalDateTime departureTime;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "is_late")
    @Builder.Default
    private Boolean isLate = false;

    @Column(name = "is_excused")
    @Builder.Default
    private Boolean isExcused = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "recorded_by", length = 100)
    private String recordedBy;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    @PrePersist
    protected void onCreate() {
        if (attendanceDate == null) {
            attendanceDate = LocalDateTime.now();
        }
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // ========== MÉTODOS DE CONVENIENCIA ==========

    /**
     * Marca como presente
     */
    public void markPresent(String recordedBy) {
        this.attended = true;
        this.arrivalTime = LocalDateTime.now();
        this.recordedBy = recordedBy;
        this.updatedBy = recordedBy;
    }

    /**
     * Marca como ausente
     */
    public void markAbsent(String recordedBy, String reason) {
        this.attended = false;
        this.notes = reason;
        this.recordedBy = recordedBy;
        this.updatedBy = recordedBy;
    }

    /**
     * Verifica si llegó tarde
     */
    public boolean isLateArrival() {
        if (arrivalTime == null || classSession == null) {
            return false;
        }
        return arrivalTime.isAfter(classSession.getScheduledDate().plusMinutes(10));
    }

    /**
     * Obtiene el tiempo de permanencia en clase
     */
    public Long getClassDurationMinutes() {
        if (arrivalTime == null || departureTime == null) {
            return null;
        }
        return java.time.Duration.between(arrivalTime, departureTime).toMinutes();
    }

    // Método getter adicional para compatibilidad
    public Boolean getAttended() {
        return attended;
    }
}