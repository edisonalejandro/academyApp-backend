package com.edidev.academyApp.dto;

import com.edidev.academyApp.enums.StudentCategory;
import com.edidev.academyApp.enums.StudentStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentDTO {

    private Long id;

    // ===== INFORMACIÓN PERSONAL =====
    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100)
    private String firstName;

    @NotBlank(message = "El apellido es obligatorio")
    @Size(max = 100)
    private String lastName;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email debe tener un formato válido")
    @Size(max = 150)
    private String email;

    @Size(max = 20)
    private String phone;

    private String emergencyContact;
    private String emergencyPhone;
    private LocalDate dateOfBirth;
    private String address;

    // ===== INFORMACIÓN ACADÉMICA =====
    private StudentCategory category;
    private StudentStatus status;

    private String universityName;
    private String studentId;
    private String career;
    private Integer semester;

    // ===== INFORMACIÓN MÉDICA/FÍSICA =====
    private String medicalConditions;
    private String allergies;
    private String medications;
    private String danceExperience;
    private String fitnessLevel;
    private String physicalLimitations;

    // ===== PREFERENCIAS =====
    private String preferredContactMethod;
    private Boolean newsletterSubscription;
    private Boolean promotionalEmails;
    private String notes;

    // ===== RELACIÓN CON USUARIO =====
    private Long userId;
    private String userEmail;

    // ===== AUDITORÍA (solo lectura) =====
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ===== CAMPO CALCULADO =====
    private String fullName;
}
