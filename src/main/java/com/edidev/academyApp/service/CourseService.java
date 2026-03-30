package com.edidev.academyApp.service;

import com.edidev.academyApp.dto.CourseDTO;
import com.edidev.academyApp.enums.DanceLevel;
import com.edidev.academyApp.enums.DanceType;
import com.edidev.academyApp.exception.DuplicateResourceException;
import com.edidev.academyApp.exception.ResourceNotFoundException;
import com.edidev.academyApp.model.Course;
import com.edidev.academyApp.model.User;
import com.edidev.academyApp.repository.CourseRepository;
import com.edidev.academyApp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CourseService {

    private final CourseRepository courseRepository;
    private final UserRepository userRepository;

    // ===================== MÉTODOS DE CONSULTA (DTO) =====================

    public List<CourseDTO> getAllActiveCourses() {
        return courseRepository.findByIsActiveTrueOrderByTitleAsc()
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    public Page<CourseDTO> getAllActiveCourses(Pageable pageable) {
        return courseRepository.findByIsActiveTrue(pageable).map(this::toDTO);
    }

    public List<CourseDTO> getAllCourses() {
        return courseRepository.findAll()
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    public Page<CourseDTO> getAllCourses(Pageable pageable) {
        return courseRepository.findAll(pageable).map(this::toDTO);
    }

    public CourseDTO getCourseById(Long courseId) {
        return toDTO(findById(courseId));
    }

    public List<CourseDTO> searchCourses(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return getAllActiveCourses();
        }
        return courseRepository.searchActiveCoursesIgnoreCase(searchTerm.trim())
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    public List<CourseDTO> getCoursesByDanceType(DanceType danceType) {
        return courseRepository.findByDanceTypeAndIsActiveTrueOrderByLevelAsc(danceType)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    public List<CourseDTO> getCoursesByLevel(DanceLevel level) {
        return courseRepository.findByLevelAndIsActiveTrueOrderByTitleAsc(level)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    public List<CourseDTO> getCoursesByTeacher(Long teacherId) {
        return courseRepository.findByTeacherIdAndIsActiveTrueOrderByTitleAsc(teacherId)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    public long getAvailableCapacity(Long courseId) {
        Course course = findById(courseId);
        Long activeEnrollments = courseRepository.countActiveEnrollmentsByCourseId(courseId);
        long enrolled = activeEnrollments != null ? activeEnrollments : 0L;
        return Math.max(0, course.getMaxCapacity() - enrolled);
    }

    // ===================== MÉTODOS DE ESCRITURA =====================

    @Transactional
    public CourseDTO createCourse(CourseDTO dto) {
        if (courseRepository.existsByCodeAndIsActiveTrue(dto.getCode())) {
            throw new DuplicateResourceException("Ya existe un curso activo con el código: " + dto.getCode());
        }
        User teacher = userRepository.findById(dto.getTeacherId())
                .orElseThrow(() -> new ResourceNotFoundException("Profesor no encontrado: " + dto.getTeacherId()));

        Course course = toEntity(dto, teacher);
        Course saved = courseRepository.save(course);
        log.info("Curso creado: {} ({})", saved.getTitle(), saved.getCode());
        return toDTO(saved);
    }

    @Transactional
    public CourseDTO updateCourse(Long courseId, CourseDTO dto) {
        Course existing = findById(courseId);

        // Si cambia el código, verificar que no exista en otro curso activo
        if (!existing.getCode().equals(dto.getCode())
                && courseRepository.existsByCodeAndIsActiveTrue(dto.getCode())) {
            throw new DuplicateResourceException("Ya existe un curso activo con el código: " + dto.getCode());
        }

        User teacher = userRepository.findById(dto.getTeacherId())
                .orElseThrow(() -> new ResourceNotFoundException("Profesor no encontrado: " + dto.getTeacherId()));

        existing.setTitle(dto.getTitle());
        existing.setCode(dto.getCode());
        existing.setDescription(dto.getDescription());
        existing.setDanceType(dto.getDanceType());
        existing.setLevel(dto.getLevel());
        existing.setPricePerHour(dto.getPricePerHour());
        if (dto.getDurationHours() != null) existing.setDurationHours(dto.getDurationHours());
        if (dto.getMaxCapacity() != null) existing.setMaxCapacity(dto.getMaxCapacity());
        existing.setTeacher(teacher);
        existing.setImageUrl(dto.getImageUrl());
        existing.setPrerequisites(dto.getPrerequisites());
        existing.setObjectives(dto.getObjectives());

        Course saved = courseRepository.save(existing);
        log.info("Curso actualizado: {} ({})", saved.getTitle(), saved.getCode());
        return toDTO(saved);
    }

    @Transactional
    public void deleteCourse(Long courseId) {
        Course course = findById(courseId);
        course.setIsActive(false);
        courseRepository.save(course);
        log.info("Curso eliminado lógicamente: {} ({})", course.getTitle(), course.getCode());
    }

    @Transactional
    public CourseDTO toggleCourseStatus(Long courseId) {
        Course course = findById(courseId);
        course.setIsActive(!course.getIsActive());
        Course saved = courseRepository.save(course);
        log.info("Estado del curso {} cambiado a: {}", saved.getCode(), saved.getIsActive());
        return toDTO(saved);
    }

    // ===================== MÉTODOS INTERNOS (sin DTO) =====================

    public Course findById(Long courseId) {
        return courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Curso no encontrado: " + courseId));
    }

    public Course findByCode(String code) {
        return courseRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Curso no encontrado con código: " + code));
    }

    public List<Course> findAllActiveCourses() {
        return courseRepository.findByIsActiveTrueOrderByTitleAsc();
    }

    public boolean existsAndIsActive(Long courseId) {
        return courseRepository.findById(courseId)
                .map(Course::getIsActive)
                .orElse(false);
    }

    @Transactional
    public Course createCourse(Course course) {
        log.info("Creando nuevo curso: {}", course.getTitle());
        return courseRepository.save(course);
    }

    @Transactional
    public Course updateCourse(Course course) {
        log.info("Actualizando curso: {}", course.getTitle());
        return courseRepository.save(course);
    }

    // ===================== MAPEOS =====================

    public CourseDTO toDTO(Course course) {
        Long activeEnrollments = courseRepository.countActiveEnrollmentsByCourseId(course.getId());
        long enrolled = activeEnrollments != null ? activeEnrollments : 0L;
        int available = Math.max(0, course.getMaxCapacity() - (int) enrolled);

        return CourseDTO.builder()
                .id(course.getId())
                .title(course.getTitle())
                .code(course.getCode())
                .description(course.getDescription())
                .danceType(course.getDanceType())
                .level(course.getLevel())
                .pricePerHour(course.getPricePerHour())
                .durationHours(course.getDurationHours())
                .maxCapacity(course.getMaxCapacity())
                .teacherId(course.getTeacher() != null ? course.getTeacher().getId() : null)
                .teacherName(course.getTeacher() != null
                        ? course.getTeacher().getFirstName() + " " + course.getTeacher().getLastName()
                        : null)
                .teacherEmail(course.getTeacher() != null ? course.getTeacher().getEmail() : null)
                .isActive(course.getIsActive())
                .imageUrl(course.getImageUrl())
                .prerequisites(course.getPrerequisites())
                .objectives(course.getObjectives())
                .activeEnrollments(enrolled)
                .availableSlots(available)
                .createdAt(course.getCreatedAt())
                .updatedAt(course.getUpdatedAt())
                .build();
    }

    private Course toEntity(CourseDTO dto, User teacher) {
        return Course.builder()
                .title(dto.getTitle())
                .code(dto.getCode())
                .description(dto.getDescription())
                .danceType(dto.getDanceType())
                .level(dto.getLevel())
                .pricePerHour(dto.getPricePerHour())
                .durationHours(dto.getDurationHours() != null ? dto.getDurationHours() : java.math.BigDecimal.valueOf(1.5))
                .maxCapacity(dto.getMaxCapacity() != null ? dto.getMaxCapacity() : 20)
                .teacher(teacher)
                .isActive(true)
                .imageUrl(dto.getImageUrl())
                .prerequisites(dto.getPrerequisites())
                .objectives(dto.getObjectives())
                .build();
    }

}