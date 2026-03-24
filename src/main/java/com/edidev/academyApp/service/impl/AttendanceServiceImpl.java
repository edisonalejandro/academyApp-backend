package com.edidev.academyApp.service.impl;

import com.edidev.academyApp.dto.AttendanceDTO;
import com.edidev.academyApp.dto.AttendanceReportDTO;
import com.edidev.academyApp.exception.ResourceNotFoundException;
import com.edidev.academyApp.model.Attendance;
import com.edidev.academyApp.model.ClassSession;
import com.edidev.academyApp.model.Student;
import com.edidev.academyApp.repository.AttendanceRepository;
import com.edidev.academyApp.repository.ClassSessionRepository;
import com.edidev.academyApp.repository.StudentRepository;
import com.edidev.academyApp.service.AttendanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AttendanceServiceImpl implements AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final StudentRepository studentRepository;
    private final ClassSessionRepository classSessionRepository;

    // =========================================================
    // IMPLEMENTACIÓN DEL CONTRATO AttendanceService
    // =========================================================

    @Override
    public Attendance recordAttendance(Long studentId, Long classId, Boolean attended, String notes) {
        log.info("Registrando asistencia: estudiante={}, sesión={}, presente={}", studentId, classId, attended);

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Estudiante no encontrado: " + studentId));

        ClassSession session = classSessionRepository.findById(classId)
                .orElseThrow(() -> new ResourceNotFoundException("Sesión no encontrada: " + classId));

        // Evitar duplicados
        Attendance existing = attendanceRepository.findByStudentIdAndClassSessionId(studentId, classId)
                .orElse(null);

        if (existing != null) {
            log.info("Actualizando registro de asistencia existente ID: {}", existing.getId());
            existing.setAttended(attended != null ? attended : false);
            existing.setNotes(notes);
            existing.setAttendanceDate(LocalDateTime.now());
            return attendanceRepository.save(existing);
        }

        Attendance attendance = Attendance.builder()
                .student(student)
                .classSession(session)
                .attended(attended != null ? attended : false)
                .attendanceDate(LocalDateTime.now())
                .notes(notes)
                .isLate(false)
                .isExcused(false)
                .build();

        return attendanceRepository.save(attendance);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Attendance> getStudentAttendances(Long studentId) {
        return attendanceRepository.findByStudentIdOrderByAttendanceDateDesc(studentId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Attendance> getClassAttendances(Long classId) {
        return attendanceRepository.findByClassSessionIdOrderByAttendanceDateDesc(classId);
    }

    @Override
    @Transactional(readOnly = true)
    public Double calculateStudentAttendanceRate(Long studentId) {
        Double rate = attendanceRepository.getAttendancePercentageByStudentId(studentId);
        return rate != null ? rate : 0.0;
    }

    @Override
    @Transactional(readOnly = true)
    public AttendanceReportDTO generateAttendanceReport(LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Generando reporte de asistencia: {} - {}", startDate, endDate);

        List<Map<String, Object>> courseStatsRaw = attendanceRepository.getAttendanceReportByCourse(startDate, endDate);
        List<Map<String, Object>> studentStatsRaw = attendanceRepository.getAttendanceReportByStudent(startDate, endDate);

        List<AttendanceReportDTO.CourseAttendanceStats> courseStats = courseStatsRaw.stream()
                .map(m -> AttendanceReportDTO.CourseAttendanceStats.builder()
                        .courseId(toLong(m.get("courseId")))
                        .courseName((String) m.get("courseName"))
                        .totalSessions(toLong(m.get("totalRecords")))
                        .totalAttendances(toLong(m.get("totalAttendances")))
                        .totalAbsences(toLong(m.get("totalAbsences")))
                        .attendanceRate(toDouble(m.get("attendancePercentage")))
                        .build())
                .collect(Collectors.toList());

        List<AttendanceReportDTO.StudentAttendanceStats> studentStats = studentStatsRaw.stream()
                .map(m -> AttendanceReportDTO.StudentAttendanceStats.builder()
                        .studentId(toLong(m.get("studentId")))
                        .firstName((String) m.get("firstName"))
                        .lastName((String) m.get("lastName"))
                        .email((String) m.get("email"))
                        .totalClasses(toLong(m.get("totalClasses")))
                        .classesAttended(toLong(m.get("classesAttended")))
                        .classesMissed(toLong(m.get("classesMissed")))
                        .attendanceRate(toDouble(m.get("attendanceRate")))
                        .riskLevel(calcRiskLevel(toDouble(m.get("attendanceRate"))))
                        .build())
                .collect(Collectors.toList());

        long totalAttendances = studentStats.stream()
                .mapToLong(s -> s.getClassesAttended() != null ? s.getClassesAttended() : 0L).sum();
        long totalAbsences = studentStats.stream()
                .mapToLong(s -> s.getClassesMissed() != null ? s.getClassesMissed() : 0L).sum();
        long totalRecords = totalAttendances + totalAbsences;
        double overallRate = totalRecords > 0 ? (double) totalAttendances / totalRecords * 100 : 0.0;

        List<AttendanceReportDTO.StudentAttendanceStats> lowAttendance = studentStats.stream()
                .filter(s -> s.getAttendanceRate() != null && s.getAttendanceRate() < 75.0)
                .collect(Collectors.toList());

        List<AttendanceReportDTO.StudentAttendanceStats> topAttendance = studentStats.stream()
                .filter(s -> s.getAttendanceRate() != null && s.getAttendanceRate() >= 90.0)
                .collect(Collectors.toList());

        return AttendanceReportDTO.builder()
                .reportDate(LocalDateTime.now())
                .periodStart(startDate)
                .periodEnd(endDate)
                .totalClasses((long) courseStats.size())
                .totalAttendances(totalAttendances)
                .totalAbsences(totalAbsences)
                .overallAttendanceRate(overallRate)
                .courseStats(courseStats)
                .studentStats(studentStats)
                .lowAttendanceStudents(lowAttendance)
                .topAttendanceStudents(topAttendance)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getStudentsWithLowAttendance(Double minPercentage) {
        LocalDateTime end = LocalDateTime.now();
        LocalDateTime start = end.minusMonths(3);
        return attendanceRepository.getStudentsWithLowAttendance(start, end, minPercentage);
    }

    @Override
    public Attendance updateAttendance(Long attendanceId, Boolean attended, String notes) {
        log.info("Actualizando asistencia ID: {}", attendanceId);

        Attendance attendance = attendanceRepository.findById(attendanceId)
                .orElseThrow(() -> new ResourceNotFoundException("Asistencia no encontrada: " + attendanceId));

        if (attended != null) attendance.setAttended(attended);
        if (notes != null) attendance.setNotes(notes);

        return attendanceRepository.save(attendance);
    }

    @Override
    @Transactional(readOnly = true)
    public Boolean hasAttendanceRecord(Long studentId, Long classId) {
        return attendanceRepository.existsAttendanceRecord(studentId, classId);
    }

    // =========================================================
    // MÉTODOS PÚBLICOS QUE DEVUELVEN DTOs (para el controller)
    // =========================================================

    public AttendanceDTO recordAttendanceDTO(Long studentId, Long classId, Boolean attended, String notes) {
        return toDTO(recordAttendance(studentId, classId, attended, notes));
    }

    @Transactional(readOnly = true)
    public List<AttendanceDTO> getStudentAttendanceDTOs(Long studentId) {
        return getStudentAttendances(studentId).stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AttendanceDTO> getClassAttendanceDTOs(Long classId) {
        return getClassAttendances(classId).stream().map(this::toDTO).collect(Collectors.toList());
    }

    public AttendanceDTO updateAttendanceDTO(Long attendanceId, Boolean attended, String notes) {
        return toDTO(updateAttendance(attendanceId, attended, notes));
    }

    // =========================================================
    // HELPERS
    // =========================================================

    public AttendanceDTO toDTO(Attendance a) {
        Student s = a.getStudent();
        ClassSession cs = a.getClassSession();

        return AttendanceDTO.builder()
                .id(a.getId())
                .studentId(s != null ? s.getId() : null)
                .studentName(s != null ? s.getFirstName() + " " + s.getLastName() : null)
                .studentEmail(s != null ? s.getEmail() : null)
                .classSessionId(cs != null ? cs.getId() : null)
                .sessionName(cs != null ? cs.getSessionName() : null)
                .scheduledDate(cs != null ? cs.getScheduledDate() : null)
                .courseId(cs != null && cs.getCourse() != null ? cs.getCourse().getId() : null)
                .courseName(cs != null && cs.getCourse() != null ? cs.getCourse().getTitle() : null)
                .attended(a.getAttended())
                .isLate(a.getIsLate())
                .isExcused(a.getIsExcused())
                .attendanceDate(a.getAttendanceDate())
                .arrivalTime(a.getArrivalTime())
                .departureTime(a.getDepartureTime())
                .notes(a.getNotes())
                .recordedBy(a.getRecordedBy())
                .createdAt(a.getCreatedAt())
                .updatedAt(a.getUpdatedAt())
                .build();
    }

    private String calcRiskLevel(Double rate) {
        if (rate == null) return "HIGH";
        if (rate >= 80) return "LOW";
        if (rate >= 60) return "MEDIUM";
        return "HIGH";
    }

    private Long toLong(Object val) {
        if (val == null) return 0L;
        if (val instanceof Long) return (Long) val;
        if (val instanceof Number) return ((Number) val).longValue();
        return 0L;
    }

    private Double toDouble(Object val) {
        if (val == null) return 0.0;
        if (val instanceof Double) return (Double) val;
        if (val instanceof Number) return ((Number) val).doubleValue();
        return 0.0;
    }
}
