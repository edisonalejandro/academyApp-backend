package com.edidev.academyApp.service;

import com.edidev.academyApp.dto.StudentDTO;
import com.edidev.academyApp.enums.StudentCategory;
import com.edidev.academyApp.enums.StudentStatus;
import com.edidev.academyApp.exception.DuplicateResourceException;
import com.edidev.academyApp.exception.ResourceNotFoundException;
import com.edidev.academyApp.model.Student;
import com.edidev.academyApp.model.User;
import com.edidev.academyApp.repository.StudentRepository;
import com.edidev.academyApp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class StudentService {

    private final StudentRepository studentRepository;
    private final UserRepository userRepository;

    // ===================== CONSULTAS =====================

    public List<StudentDTO> getAllStudents() {
        return studentRepository.findAllByOrderByLastNameAscFirstNameAsc()
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    public Page<StudentDTO> getAllStudents(Pageable pageable) {
        return studentRepository.findAll(pageable).map(this::toDTO);
    }

    public StudentDTO getStudentById(Long id) {
        return toDTO(findById(id));
    }

    public StudentDTO getStudentByUserId(Long userId) {
        Student student = studentRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No hay perfil de estudiante para el usuario: " + userId));
        return toDTO(student);
    }

    public StudentDTO getStudentByEmail(String email) {
        Student student = studentRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Estudiante no encontrado con email: " + email));
        return toDTO(student);
    }

    public List<StudentDTO> searchStudents(String term) {
        if (term == null || term.trim().isEmpty()) {
            return getAllStudents();
        }
        return studentRepository.searchByTerm(term.trim())
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    public List<StudentDTO> getStudentsByStatus(StudentStatus status) {
        return studentRepository.findByStatusOrderByLastNameAsc(status)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    public List<StudentDTO> getStudentsByCategory(StudentCategory category) {
        return studentRepository.findByCategoryOrderByLastNameAsc(category)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    // ===================== ESCRITURA =====================

    @Transactional
    public StudentDTO createStudent(StudentDTO dto) {
        if (studentRepository.existsByEmail(dto.getEmail())) {
            throw new DuplicateResourceException("Ya existe un estudiante con el email: " + dto.getEmail());
        }

        User user = null;
        if (dto.getUserId() != null) {
            user = userRepository.findById(dto.getUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + dto.getUserId()));
        }

        Student student = toEntity(dto, user);
        Student saved = studentRepository.save(student);
        log.info("Estudiante creado: {} {} ({})", saved.getFirstName(), saved.getLastName(), saved.getEmail());
        return toDTO(saved);
    }

    @Transactional
    public StudentDTO updateStudent(Long id, StudentDTO dto) {
        Student existing = findById(id);

        // Verificar email único si cambia
        if (!existing.getEmail().equals(dto.getEmail())
                && studentRepository.existsByEmail(dto.getEmail())) {
            throw new DuplicateResourceException("Ya existe un estudiante con el email: " + dto.getEmail());
        }

        existing.setFirstName(dto.getFirstName());
        existing.setLastName(dto.getLastName());
        existing.setEmail(dto.getEmail());
        existing.setPhone(dto.getPhone());
        existing.setEmergencyContact(dto.getEmergencyContact());
        existing.setEmergencyPhone(dto.getEmergencyPhone());
        existing.setDateOfBirth(dto.getDateOfBirth());
        existing.setAddress(dto.getAddress());
        if (dto.getCategory() != null) existing.setCategory(dto.getCategory());
        existing.setUniversityName(dto.getUniversityName());
        existing.setStudentId(dto.getStudentId());
        existing.setCareer(dto.getCareer());
        existing.setSemester(dto.getSemester());
        existing.setMedicalConditions(dto.getMedicalConditions());
        existing.setAllergies(dto.getAllergies());
        existing.setMedications(dto.getMedications());
        existing.setDanceExperience(dto.getDanceExperience());
        existing.setFitnessLevel(dto.getFitnessLevel());
        existing.setPhysicalLimitations(dto.getPhysicalLimitations());
        if (dto.getPreferredContactMethod() != null) existing.setPreferredContactMethod(dto.getPreferredContactMethod());
        if (dto.getNewsletterSubscription() != null) existing.setNewsletterSubscription(dto.getNewsletterSubscription());
        if (dto.getPromotionalEmails() != null) existing.setPromotionalEmails(dto.getPromotionalEmails());
        existing.setNotes(dto.getNotes());

        Student saved = studentRepository.save(existing);
        log.info("Estudiante actualizado: {} {}", saved.getFirstName(), saved.getLastName());
        return toDTO(saved);
    }

    @Transactional
    public StudentDTO updateCategory(Long id, StudentCategory category) {
        Student student = findById(id);
        student.setCategory(category);
        Student saved = studentRepository.save(student);
        log.info("Categoría del estudiante {} actualizada a: {}", id, category);
        return toDTO(saved);
    }

    // ===================== MÉTODOS INTERNOS =====================

    public Student findById(Long id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Estudiante no encontrado: " + id));
    }

    // ===================== MAPEOS =====================

    public StudentDTO toDTO(Student student) {
        return StudentDTO.builder()
                .id(student.getId())
                .firstName(student.getFirstName())
                .lastName(student.getLastName())
                .email(student.getEmail())
                .phone(student.getPhone())
                .emergencyContact(student.getEmergencyContact())
                .emergencyPhone(student.getEmergencyPhone())
                .dateOfBirth(student.getDateOfBirth())
                .address(student.getAddress())
                .category(student.getCategory())
                .status(student.getStatus())
                .universityName(student.getUniversityName())
                .studentId(student.getStudentId())
                .career(student.getCareer())
                .semester(student.getSemester())
                .medicalConditions(student.getMedicalConditions())
                .allergies(student.getAllergies())
                .medications(student.getMedications())
                .danceExperience(student.getDanceExperience())
                .fitnessLevel(student.getFitnessLevel())
                .physicalLimitations(student.getPhysicalLimitations())
                .preferredContactMethod(student.getPreferredContactMethod())
                .newsletterSubscription(student.getNewsletterSubscription())
                .promotionalEmails(student.getPromotionalEmails())
                .notes(student.getNotes())
                .userId(student.getUser() != null ? student.getUser().getId() : null)
                .userEmail(student.getUser() != null ? student.getUser().getEmail() : null)
                .fullName(student.getFirstName() + " " + student.getLastName())
                .createdAt(student.getCreatedAt())
                .updatedAt(student.getUpdatedAt())
                .build();
    }

    private Student toEntity(StudentDTO dto, User user) {
        return Student.builder()
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .email(dto.getEmail())
                .phone(dto.getPhone())
                .emergencyContact(dto.getEmergencyContact())
                .emergencyPhone(dto.getEmergencyPhone())
                .dateOfBirth(dto.getDateOfBirth())
                .address(dto.getAddress())
                .category(dto.getCategory() != null ? dto.getCategory() : StudentCategory.REGULAR)
                .status(StudentStatus.ACTIVE)
                .universityName(dto.getUniversityName())
                .studentId(dto.getStudentId())
                .career(dto.getCareer())
                .semester(dto.getSemester())
                .medicalConditions(dto.getMedicalConditions())
                .allergies(dto.getAllergies())
                .medications(dto.getMedications())
                .danceExperience(dto.getDanceExperience())
                .fitnessLevel(dto.getFitnessLevel())
                .physicalLimitations(dto.getPhysicalLimitations())
                .preferredContactMethod(dto.getPreferredContactMethod() != null
                        ? dto.getPreferredContactMethod() : "EMAIL")
                .newsletterSubscription(dto.getNewsletterSubscription() != null
                        ? dto.getNewsletterSubscription() : true)
                .promotionalEmails(dto.getPromotionalEmails() != null
                        ? dto.getPromotionalEmails() : true)
                .notes(dto.getNotes())
                .user(user)
                .build();
    }
}
