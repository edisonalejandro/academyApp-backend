package com.edidev.academyApp.service;

import com.edidev.academyApp.dto.ClassSessionDTO;
import com.edidev.academyApp.enums.ClassStatus;
import com.edidev.academyApp.exception.ResourceNotFoundException;
import com.edidev.academyApp.model.ClassSession;
import com.edidev.academyApp.model.Course;
import com.edidev.academyApp.model.User;
import com.edidev.academyApp.repository.AttendanceRepository;
import com.edidev.academyApp.repository.ClassSessionRepository;
import com.edidev.academyApp.repository.CourseRepository;
import com.edidev.academyApp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ClassSessionService {

    private final ClassSessionRepository classSessionRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final AttendanceRepository attendanceRepository;

    // =========================================================
    // CRUD
    // =========================================================

    public ClassSessionDTO createSession(ClassSessionDTO dto) {
        log.info("Creando sesión: {}", dto.getSessionName());

        Course course = courseRepository.findById(dto.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Curso no encontrado: " + dto.getCourseId()));

        User teacher = userRepository.findById(dto.getTeacherId())
                .orElseThrow(() -> new ResourceNotFoundException("Profesor no encontrado: " + dto.getTeacherId()));

        ClassSession session = ClassSession.builder()
                .course(course)
                .teacher(teacher)
                .sessionName(dto.getSessionName())
                .description(dto.getDescription())
                .scheduledDate(dto.getScheduledDate())
                .plannedDuration(dto.getPlannedDuration() != null ? dto.getPlannedDuration() : BigDecimal.valueOf(1.5))
                .status(ClassStatus.SCHEDULED)
                .maxCapacity(dto.getMaxCapacity() != null ? dto.getMaxCapacity() : 20)
                .location(dto.getLocation())
                .topic(dto.getTopic())
                .requiredMaterials(dto.getRequiredMaterials())
                .teacherNotes(dto.getTeacherNotes())
                .virtualMeetingUrl(dto.getVirtualMeetingUrl())
                .isVirtual(dto.getIsVirtual() != null ? dto.getIsVirtual() : false)
                .isRecurring(dto.getIsRecurring() != null ? dto.getIsRecurring() : false)
                .difficultyLevel(dto.getDifficultyLevel())
                .specialRequirements(dto.getSpecialRequirements())
                .build();

        ClassSession saved = classSessionRepository.save(session);
        log.info("Sesión creada con ID: {}", saved.getId());
        return toDTO(saved);
    }

    @Transactional(readOnly = true)
    public ClassSessionDTO getSessionById(Long id) {
        ClassSession session = findOrThrow(id);
        return toDTO(session);
    }

    public ClassSessionDTO updateSession(Long id, ClassSessionDTO dto) {
        log.info("Actualizando sesión ID: {}", id);

        ClassSession session = findOrThrow(id);

        if (dto.getCourseId() != null && !dto.getCourseId().equals(session.getCourse().getId())) {
            Course course = courseRepository.findById(dto.getCourseId())
                    .orElseThrow(() -> new ResourceNotFoundException("Curso no encontrado: " + dto.getCourseId()));
            session.setCourse(course);
        }

        if (dto.getTeacherId() != null && !dto.getTeacherId().equals(session.getTeacher().getId())) {
            User teacher = userRepository.findById(dto.getTeacherId())
                    .orElseThrow(() -> new ResourceNotFoundException("Profesor no encontrado: " + dto.getTeacherId()));
            session.setTeacher(teacher);
        }

        if (dto.getSessionName() != null) session.setSessionName(dto.getSessionName());
        if (dto.getDescription() != null) session.setDescription(dto.getDescription());
        if (dto.getScheduledDate() != null) session.setScheduledDate(dto.getScheduledDate());
        if (dto.getPlannedDuration() != null) session.setPlannedDuration(dto.getPlannedDuration());
        if (dto.getMaxCapacity() != null) session.setMaxCapacity(dto.getMaxCapacity());
        if (dto.getLocation() != null) session.setLocation(dto.getLocation());
        if (dto.getTopic() != null) session.setTopic(dto.getTopic());
        if (dto.getRequiredMaterials() != null) session.setRequiredMaterials(dto.getRequiredMaterials());
        if (dto.getTeacherNotes() != null) session.setTeacherNotes(dto.getTeacherNotes());
        if (dto.getVirtualMeetingUrl() != null) session.setVirtualMeetingUrl(dto.getVirtualMeetingUrl());
        if (dto.getIsVirtual() != null) session.setIsVirtual(dto.getIsVirtual());
        if (dto.getIsRecurring() != null) session.setIsRecurring(dto.getIsRecurring());
        if (dto.getDifficultyLevel() != null) session.setDifficultyLevel(dto.getDifficultyLevel());
        if (dto.getSpecialRequirements() != null) session.setSpecialRequirements(dto.getSpecialRequirements());

        return toDTO(classSessionRepository.save(session));
    }

    // =========================================================
    // CONSULTAS
    // =========================================================

    @Transactional(readOnly = true)
    public List<ClassSessionDTO> getSessionsByCourse(Long courseId) {
        return classSessionRepository.findByCourseIdOrderByScheduledDateAsc(courseId)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ClassSessionDTO> getUpcomingSessions(Long courseId) {
        return classSessionRepository
                .findByCourseAndStatus(courseId, ClassStatus.SCHEDULED)
                .stream()
                .filter(s -> s.getScheduledDate().isAfter(LocalDateTime.now()))
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ClassSessionDTO> getSessionsByDateRange(LocalDateTime from, LocalDateTime to) {
        return classSessionRepository.findByDateRange(from, to)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ClassSessionDTO> getSessionsByTeacher(Long teacherId) {
        return classSessionRepository.findByTeacherIdOrderByScheduledDateDesc(teacherId)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ClassSessionDTO> getAllSessions() {
        return classSessionRepository.findAll()
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    // =========================================================
    // CAMBIOS DE ESTADO
    // =========================================================

    public ClassSessionDTO cancelSession(Long id, String reason) {
        log.info("Cancelando sesión ID: {}", id);
        ClassSession session = findOrThrow(id);
        session.setStatus(ClassStatus.CANCELLED);
        session.setCancellationReason(reason);
        return toDTO(classSessionRepository.save(session));
    }

    public ClassSessionDTO startSession(Long id) {
        log.info("Iniciando sesión ID: {}", id);
        ClassSession session = findOrThrow(id);
        session.setStatus(ClassStatus.IN_PROGRESS);
        session.setActualStartTime(LocalDateTime.now());
        return toDTO(classSessionRepository.save(session));
    }

    public ClassSessionDTO completeSession(Long id) {
        log.info("Completando sesión ID: {}", id);
        ClassSession session = findOrThrow(id);

        LocalDateTime end = LocalDateTime.now();
        session.setActualEndTime(end);
        session.setStatus(ClassStatus.COMPLETED);

        if (session.getActualStartTime() != null) {
            long minutes = ChronoUnit.MINUTES.between(session.getActualStartTime(), end);
            session.setActualDuration(BigDecimal.valueOf(minutes).divide(BigDecimal.valueOf(60), 2, java.math.RoundingMode.HALF_UP));
        }

        return toDTO(classSessionRepository.save(session));
    }

    // =========================================================
    // HELPERS
    // =========================================================

    private ClassSession findOrThrow(Long id) {
        return classSessionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sesión no encontrada: " + id));
    }

    private ClassSessionDTO toDTO(ClassSession s) {
        Long attendanceCount = attendanceRepository.countByClassSessionId(s.getId());

        Integer availableSpots = null;
        if (s.getMaxCapacity() != null && attendanceCount != null) {
            availableSpots = s.getMaxCapacity() - attendanceCount.intValue();
        }

        return ClassSessionDTO.builder()
                .id(s.getId())
                .courseId(s.getCourse() != null ? s.getCourse().getId() : null)
                .courseName(s.getCourse() != null ? s.getCourse().getTitle() : null)
                .courseCode(s.getCourse() != null ? s.getCourse().getCode() : null)
                .teacherId(s.getTeacher() != null ? s.getTeacher().getId() : null)
                .teacherName(s.getTeacher() != null
                        ? s.getTeacher().getFirstName() + " " + s.getTeacher().getLastName() : null)
                .teacherEmail(s.getTeacher() != null ? s.getTeacher().getEmail() : null)
                .sessionName(s.getSessionName())
                .description(s.getDescription())
                .scheduledDate(s.getScheduledDate())
                .actualStartTime(s.getActualStartTime())
                .actualEndTime(s.getActualEndTime())
                .plannedDuration(s.getPlannedDuration())
                .actualDuration(s.getActualDuration())
                .status(s.getStatus())
                .maxCapacity(s.getMaxCapacity())
                .location(s.getLocation())
                .topic(s.getTopic())
                .requiredMaterials(s.getRequiredMaterials())
                .teacherNotes(s.getTeacherNotes())
                .virtualMeetingUrl(s.getVirtualMeetingUrl())
                .isVirtual(s.getIsVirtual())
                .isRecurring(s.getIsRecurring())
                .parentClassId(s.getParentClassId())
                .difficultyLevel(s.getDifficultyLevel())
                .specialRequirements(s.getSpecialRequirements())
                .cancellationReason(s.getCancellationReason())
                .createdAt(s.getCreatedAt())
                .updatedAt(s.getUpdatedAt())
                .attendanceCount(attendanceCount)
                .availableSpots(availableSpots)
                .build();
    }
}
