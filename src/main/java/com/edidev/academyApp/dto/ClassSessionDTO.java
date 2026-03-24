package com.edidev.academyApp.dto;

import com.edidev.academyApp.enums.ClassStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClassSessionDTO {

    private Long id;

    // Curso
    private Long courseId;
    private String courseName;
    private String courseCode;

    // Profesor
    private Long teacherId;
    private String teacherName;
    private String teacherEmail;

    // Datos de la sesión
    private String sessionName;
    private String description;
    private LocalDateTime scheduledDate;
    private LocalDateTime actualStartTime;
    private LocalDateTime actualEndTime;
    private BigDecimal plannedDuration;
    private BigDecimal actualDuration;
    private ClassStatus status;

    // Capacidad y ubicación
    private Integer maxCapacity;
    private String location;
    private String topic;
    private String requiredMaterials;
    private String teacherNotes;

    // Virtual
    private String virtualMeetingUrl;
    private Boolean isVirtual;

    // Recurrencia
    private Boolean isRecurring;
    private Long parentClassId;

    // Extra info
    private String difficultyLevel;
    private String specialRequirements;
    private String cancellationReason;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Calculados
    private Long attendanceCount;
    private Integer availableSpots;
}
