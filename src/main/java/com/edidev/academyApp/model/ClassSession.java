package com.edidev.academyApp.model;

import com.edidev.academyApp.enums.ClassStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "class_sessions", indexes = {
    @Index(name = "idx_class_scheduled_date", columnList = "scheduled_date"),
    @Index(name = "idx_class_status", columnList = "status"),
    @Index(name = "idx_class_course_id", columnList = "course_id"),
    @Index(name = "idx_class_teacher_id", columnList = "teacher_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(exclude = {"course", "teacher", "attendances"})
@ToString(exclude = {"course", "teacher", "attendances"})
public class ClassSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", nullable = false)
    private User teacher;

    @Column(name = "session_name", nullable = false, length = 150)
    private String sessionName;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "scheduled_date", nullable = false)
    private LocalDateTime scheduledDate;

    @Column(name = "actual_start_time")
    private LocalDateTime actualStartTime;

    @Column(name = "actual_end_time")
    private LocalDateTime actualEndTime;

    @Column(name = "planned_duration", nullable = false, precision = 3, scale = 2)
    @Builder.Default
    private BigDecimal plannedDuration = BigDecimal.valueOf(1.5);

    @Column(name = "actual_duration", precision = 3, scale = 2)
    private BigDecimal actualDuration;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private ClassStatus status = ClassStatus.SCHEDULED;

    @Column(name = "max_capacity")
    @Builder.Default
    private Integer maxCapacity = 20;

    @Column(name = "location", length = 200)
    private String location;

    @Column(name = "topic", length = 300)
    private String topic;

    @Column(name = "required_materials", columnDefinition = "TEXT")
    private String requiredMaterials;

    @Column(name = "teacher_notes", columnDefinition = "TEXT")
    private String teacherNotes;

    @Column(name = "virtual_meeting_url", length = 500)
    private String virtualMeetingUrl;

    @Column(name = "meeting_id", length = 100)
    private String meetingId;

    @Column(name = "meeting_password", length = 50)
    private String meetingPassword;

    @Column(name = "is_virtual", nullable = false)
    @Builder.Default
    private Boolean isVirtual = false;

    @Column(name = "is_recurring", nullable = false)
    @Builder.Default
    private Boolean isRecurring = false;

    @Column(name = "parent_class_id")
    private Long parentClassId;

    @Column(name = "difficulty_level", length = 50)
    private String difficultyLevel;

    @Column(name = "special_requirements", columnDefinition = "TEXT")
    private String specialRequirements;

    @Column(name = "cancellation_reason", length = 500)
    private String cancellationReason;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    @OneToMany(mappedBy = "classSession", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Attendance> attendances = new ArrayList<>();

    // ========== CALLBACKS DE JPA ==========

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
        if (sessionName == null || sessionName.isEmpty()) {
            sessionName = generateSessionName();
        }
        if (maxCapacity == null && course != null) {
            maxCapacity = course.getMaxCapacity() != null ? course.getMaxCapacity() : 20;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        if (actualStartTime != null && actualEndTime != null) {
            long minutes = java.time.Duration.between(actualStartTime, actualEndTime).toMinutes();
            actualDuration = BigDecimal.valueOf(minutes).divide(BigDecimal.valueOf(60), 2, BigDecimal.ROUND_HALF_UP);
        }
    }

    // ========== MÉTODOS DE GESTIÓN DE CLASE ==========

    private String generateSessionName() {
        if (course != null) {
            String dateStr = scheduledDate.toLocalDate().toString();
            String timeStr = scheduledDate.toLocalTime().toString().substring(0, 5);
            return String.format("%s - %s %s", course.getTitle(), dateStr, timeStr);
        }
        return "Clase " + scheduledDate.toLocalDate();
    }

    public void startClass(String startedBy) {
        this.actualStartTime = LocalDateTime.now();
        this.status = ClassStatus.IN_PROGRESS;
        this.updatedBy = startedBy;
        addTeacherNote("Clase iniciada", startedBy);
    }

    public void endClass(String endedBy) {
        this.actualEndTime = LocalDateTime.now();
        this.status = ClassStatus.COMPLETED;
        this.updatedBy = endedBy;
        if (actualStartTime != null) {
            long minutes = java.time.Duration.between(actualStartTime, actualEndTime).toMinutes();
            this.actualDuration = BigDecimal.valueOf(minutes).divide(BigDecimal.valueOf(60), 2, BigDecimal.ROUND_HALF_UP);
        }
        String note = String.format("Clase finalizada. Duración: %s horas. Asistentes: %d/%d", 
                actualDuration, getAttendanceCount(), getRegisteredCount());
        addTeacherNote(note, endedBy);
    }

    public void cancelClass(String reason, String cancelledBy) {
        this.status = ClassStatus.CANCELLED;
        this.cancellationReason = reason;
        this.updatedBy = cancelledBy;
        addTeacherNote("CANCELADA: " + reason, cancelledBy);
    }

    public int getAttendanceCount() {
        return (int) attendances.stream()
                .filter(attendance -> attendance.getAttended() != null && attendance.getAttended())
                .count();
    }

    public int getRegisteredCount() {
        return attendances != null ? attendances.size() : 0;
    }

    public boolean isFuture() {
        return scheduledDate.isAfter(LocalDateTime.now());
    }

    public boolean isPast() {
        return scheduledDate.isBefore(LocalDateTime.now());
    }

    public boolean canTakeAttendance() {
        return status == ClassStatus.IN_PROGRESS || 
               (status == ClassStatus.SCHEDULED && !isFuture());
    }

    public void addTeacherNote(String note, String addedBy) {
        String timestamp = LocalDateTime.now().toString();
        String newNote = String.format("[%s%s]: %s", 
                timestamp, 
                addedBy != null ? " - " + addedBy : "", 
                note);
        
        if (this.teacherNotes == null || this.teacherNotes.isEmpty()) {
            this.teacherNotes = newNote;
        } else {
            this.teacherNotes += "\n" + newNote;
        }
        this.updatedBy = addedBy;
    }

    public String getLogSummary() {
        return String.format("Class[id=%d, name=%s, date=%s, status=%s]",
                id, sessionName, scheduledDate, status);
    }
}