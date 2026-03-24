package com.edidev.academyApp.model;

import com.edidev.academyApp.enums.StudentCategory;
import com.edidev.academyApp.enums.StudentStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "students", indexes = {
    @Index(name = "idx_student_email", columnList = "email"),
    @Index(name = "idx_student_phone", columnList = "phone"),
    @Index(name = "idx_student_status", columnList = "status"),
    @Index(name = "idx_student_category", columnList = "category"),
    @Index(name = "idx_student_user_id", columnList = "user_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(exclude = {"enrollments", "attendances", "user"})
@ToString(exclude = {"enrollments", "attendances", "user"})
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ========== INFORMACIÓN PERSONAL ==========

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(name = "email", nullable = false, unique = true, length = 150)
    private String email;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "emergency_contact", length = 150)
    private String emergencyContact;

    @Column(name = "emergency_phone", length = 20)
    private String emergencyPhone;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "address", length = 500)
    private String address;

    // ========== INFORMACIÓN ACADÉMICA ==========

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    @Builder.Default
    private StudentCategory category = StudentCategory.REGULAR;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private StudentStatus status = StudentStatus.ACTIVE;

    @Column(name = "university_name", length = 200)
    private String universityName;

    @Column(name = "student_id", length = 50)
    private String studentId; // ID universitario si aplica

    @Column(name = "career", length = 150)
    private String career;

    @Column(name = "semester")
    private Integer semester;

    // ========== INFORMACIÓN MÉDICA/FÍSICA ==========

    @Column(name = "medical_conditions", columnDefinition = "TEXT")
    private String medicalConditions;

    @Column(name = "allergies", length = 500)
    private String allergies;

    @Column(name = "medications", columnDefinition = "TEXT")
    private String medications;

    @Column(name = "dance_experience", length = 500)
    private String danceExperience;

    @Column(name = "fitness_level", length = 100)
    private String fitnessLevel; // Principiante, Intermedio, Avanzado

    @Column(name = "physical_limitations", columnDefinition = "TEXT")
    private String physicalLimitations;

    // ========== INFORMACIÓN DE CONTACTO Y PREFERENCIAS ==========

    @Column(name = "preferred_contact_method", length = 50)
    @Builder.Default
    private String preferredContactMethod = "EMAIL"; // EMAIL, PHONE, WHATSAPP

    @Column(name = "newsletter_subscription")
    @Builder.Default
    private Boolean newsletterSubscription = true;

    @Column(name = "promotional_emails")
    @Builder.Default
    private Boolean promotionalEmails = true;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes; // Notas adicionales del staff

    // ========== RELACIONES ==========

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Enrollment> enrollments = new ArrayList<>();

    // Los pagos están relacionados con User, no directamente con Student
    // Se puede acceder a través de: student.getUser().getPayments()

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Attendance> attendances = new ArrayList<>();

    // ========== CAMPOS DE AUDITORÍA ==========

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

    // ========== MÉTODOS DE CONVENIENCIA ==========

    /**
     * Obtiene el nombre completo del estudiante
     */
    public String getFullName() {
        return firstName + " " + lastName;
    }

    /**
     * Verifica si el estudiante es universitario
     */
    public boolean isUniversityStudent() {
        return category == StudentCategory.UNIVERSITY;
    }

    /**
     * Verifica si el estudiante está activo
     */
    public boolean isActive() {
        return status == StudentStatus.ACTIVE;
    }

    /**
     * Calcula la edad del estudiante
     */
    public Integer getAge() {
        if (dateOfBirth == null) {
            return null;
        }
        return LocalDate.now().getYear() - dateOfBirth.getYear();
    }

    /**
     * Verifica si el estudiante es menor de edad
     */
    public boolean isMinor() {
        Integer age = getAge();
        return age != null && age < 18;
    }

    /**
     * Obtiene el total de pagos realizados
     * Nota: Los pagos no están directamente relacionados con Student
     * Se relacionan a través de User. Para obtener pagos, considere usar
     * un servicio que consulte Payment por user_id
     */
    public Integer getTotalPayments() {
        // TODO: Implementar consulta a través de PaymentService si es necesario
        return 0;
    }

    /**
     * Obtiene el total de cursos en los que está inscrito
     */
    public Integer getTotalEnrollments() {
        return enrollments != null ? enrollments.size() : 0;
    }

    /**
     * Verifica si tiene condiciones médicas especiales
     */
    public boolean hasMedicalConditions() {
        return medicalConditions != null && !medicalConditions.trim().isEmpty();
    }

    /**
     * Verifica si tiene limitaciones físicas
     */
    public boolean hasPhysicalLimitations() {
        return physicalLimitations != null && !physicalLimitations.trim().isEmpty();
    }

    /**
     * Obtiene el método de contacto preferido
     */
    public String getPreferredContactMethod() {
        return preferredContactMethod != null ? preferredContactMethod : "EMAIL";
    }

    /**
     * Verifica si acepta emails promocionales
     */
    public boolean acceptsPromotionalEmails() {
        return promotionalEmails != null && promotionalEmails;
    }

    /**
     * Obtiene información de contacto de emergencia
     */
    public String getEmergencyContactInfo() {
        if (emergencyContact == null && emergencyPhone == null) {
            return "No especificado";
        }
        
        StringBuilder info = new StringBuilder();
        if (emergencyContact != null) {
            info.append(emergencyContact);
        }
        if (emergencyPhone != null) {
            if (info.length() > 0) info.append(" - ");
            info.append(emergencyPhone);
        }
        
        return info.toString();
    }

    /**
     * Actualiza el estado del estudiante
     */
    public void updateStatus(StudentStatus newStatus, String updatedBy) {
        this.status = newStatus;
        this.updatedBy = updatedBy;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Actualiza la categoría del estudiante
     */
    public void updateCategory(StudentCategory newCategory, String updatedBy) {
        this.category = newCategory;
        this.updatedBy = updatedBy;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Agregar nota del staff
     */
    public void addNote(String note, String addedBy) {
        String timestamp = LocalDateTime.now().toString();
        String newNote = String.format("[%s - %s]: %s", timestamp, addedBy, note);
        
        if (this.notes == null || this.notes.isEmpty()) {
            this.notes = newNote;
        } else {
            this.notes += "\n" + newNote;
        }
        
        this.updatedBy = addedBy;
        this.updatedAt = LocalDateTime.now();
    }

    // ========== MÉTODOS DE VALIDACIÓN ==========

    /**
     * Valida si los datos básicos están completos
     */
    public boolean hasCompleteBasicInfo() {
        return firstName != null && !firstName.trim().isEmpty() &&
               lastName != null && !lastName.trim().isEmpty() &&
               email != null && !email.trim().isEmpty();
    }

    /**
     * Valida si tiene información de contacto de emergencia
     */
    public boolean hasEmergencyContact() {
        return (emergencyContact != null && !emergencyContact.trim().isEmpty()) ||
               (emergencyPhone != null && !emergencyPhone.trim().isEmpty());
    }

    /**
     * Obtiene un resumen del estudiante para logs
     */
    public String getLogSummary() {
        return String.format("Student[id=%d, name=%s, email=%s, category=%s, status=%s]",
                id, getFullName(), email, category, status);
    }

    // ========== CALLBACKS DE JPA ==========

    @PrePersist
    protected void onCreate() {
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
}