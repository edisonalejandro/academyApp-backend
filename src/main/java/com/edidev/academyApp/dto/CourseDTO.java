package com.edidev.academyApp.dto;

import com.edidev.academyApp.enums.DanceLevel;
import com.edidev.academyApp.enums.DanceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
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
public class CourseDTO {

    private Long id;

    @NotBlank(message = "El título es obligatorio")
    @Size(max = 100, message = "El título no puede superar los 100 caracteres")
    private String title;

    @NotBlank(message = "El código es obligatorio")
    @Size(max = 20, message = "El código no puede superar los 20 caracteres")
    private String code;

    private String description;

    @NotNull(message = "El tipo de baile es obligatorio")
    private DanceType danceType;

    @NotNull(message = "El nivel es obligatorio")
    private DanceLevel level;

    @Positive(message = "El precio por hora debe ser positivo")
    private BigDecimal pricePerHour;

    private BigDecimal durationHours;

    @Positive(message = "La capacidad máxima debe ser positiva")
    private Integer maxCapacity;

    @NotNull(message = "El profesor es obligatorio")
    private Long teacherId;

    // Campos de solo lectura (respuesta)
    private String teacherName;
    private String teacherEmail;

    private Boolean isActive;

    private String imageUrl;
    private String prerequisites;
    private String objectives;

    // Campos calculados (solo respuesta)
    private Long activeEnrollments;
    private Integer availableSlots;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
