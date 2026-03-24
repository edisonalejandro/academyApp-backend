package com.edidev.academyApp.repository;

import com.edidev.academyApp.model.Attendance;
import com.edidev.academyApp.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    // ========== CONSULTAS BÁSICAS ==========

    /**
     * Buscar asistencias por estudiante
     */
    List<Attendance> findByStudentIdOrderByAttendanceDateDesc(Long studentId);

    /**
     * Buscar asistencias por clase
     */
    List<Attendance> findByClassSessionIdOrderByAttendanceDateDesc(Long classSessionId);

    /**
     * Contar asistencias registradas para una sesión de clase
     */
    Long countByClassSessionId(Long classSessionId);

    /**
     * Buscar asistencias por estudiante y clase específica
     */
    Optional<Attendance> findByStudentIdAndClassSessionId(Long studentId, Long classSessionId);

    /**
     * Buscar asistencias por email de estudiante
     */
    @Query("SELECT a FROM Attendance a WHERE a.student.email = :email ORDER BY a.attendanceDate DESC")
    List<Attendance> findByStudentEmailOrderByAttendanceDateDesc(@Param("email") String email);

    // ========== CONSULTAS POR FECHA ==========

    /**
     * Asistencias en un rango de fechas
     */
    @Query("SELECT a FROM Attendance a WHERE a.attendanceDate BETWEEN :startDate AND :endDate ORDER BY a.attendanceDate DESC")
    List<Attendance> findByAttendanceDateBetween(@Param("startDate") LocalDateTime startDate,
                                                @Param("endDate") LocalDateTime endDate);

    /**
     * Asistencias de un estudiante en un rango de fechas
     */
    @Query("SELECT a FROM Attendance a WHERE a.student.id = :studentId AND a.attendanceDate BETWEEN :startDate AND :endDate ORDER BY a.attendanceDate DESC")
    List<Attendance> findByStudentIdAndAttendanceDateBetween(@Param("studentId") Long studentId,
                                                           @Param("startDate") LocalDateTime startDate,
                                                           @Param("endDate") LocalDateTime endDate);

    /**
     * Asistencias de una clase en un rango de fechas
     */
    @Query("SELECT a FROM Attendance a WHERE a.classSession.id = :classId AND a.attendanceDate BETWEEN :startDate AND :endDate ORDER BY a.attendanceDate DESC")
    List<Attendance> findByClassSessionIdAndAttendanceDateBetween(@Param("classId") Long classId,
                                                                @Param("startDate") LocalDateTime startDate,
                                                                @Param("endDate") LocalDateTime endDate);

    // ========== ESTADÍSTICAS DE ASISTENCIA ==========

    /**
     * Contar total de asistencias de un estudiante
     */
    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.student.id = :studentId AND a.attended = true")
    Long countAttendancesByStudentId(@Param("studentId") Long studentId);

    /**
     * Contar total de faltas de un estudiante
     */
    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.student.id = :studentId AND a.attended = false")
    Long countAbsencesByStudentId(@Param("studentId") Long studentId);

    /**
     * Calcular porcentaje de asistencia de un estudiante
     */
    @Query("SELECT " +
           "CAST(SUM(CASE WHEN a.attended = true THEN 1 ELSE 0 END) AS DOUBLE) / COUNT(a) * 100 " +
           "FROM Attendance a WHERE a.student.id = :studentId")
    Double getAttendancePercentageByStudentId(@Param("studentId") Long studentId);

    /**
     * Estadísticas de asistencia por clase
     */
    @Query("SELECT a.classSession.id as classId, " +
           "COUNT(a) as totalStudents, " +
           "SUM(CASE WHEN a.attended = true THEN 1 ELSE 0 END) as presentStudents, " +
           "SUM(CASE WHEN a.attended = false THEN 1 ELSE 0 END) as absentStudents " +
           "FROM Attendance a WHERE a.classSession.id = :classId GROUP BY a.classSession.id")
    Map<String, Object> getClassAttendanceStats(@Param("classId") Long classId);

    // ========== REPORTES DE ASISTENCIA ==========

    /**
     * Reporte de asistencia por curso
     */
    @Query("SELECT a.classSession.course.id as courseId, " +
           "a.classSession.course.title as courseName, " +
           "COUNT(a) as totalRecords, " +
           "SUM(CASE WHEN a.attended = true THEN 1 ELSE 0 END) as totalAttendances, " +
           "SUM(CASE WHEN a.attended = false THEN 1 ELSE 0 END) as totalAbsences, " +
           "CAST(SUM(CASE WHEN a.attended = true THEN 1 ELSE 0 END) AS DOUBLE) / COUNT(a) * 100 as attendancePercentage " +
           "FROM Attendance a " +
           "WHERE a.attendanceDate BETWEEN :startDate AND :endDate " +
           "GROUP BY a.classSession.course.id, a.classSession.course.title " +
           "ORDER BY attendancePercentage DESC")
    List<Map<String, Object>> getAttendanceReportByCourse(@Param("startDate") LocalDateTime startDate,
                                                         @Param("endDate") LocalDateTime endDate);

    /**
     * Reporte de asistencia por estudiante
     */
    @Query("SELECT s.id as studentId, " +
           "s.firstName as firstName, " +
           "s.lastName as lastName, " +
           "s.email as email, " +
           "COUNT(a) as totalClasses, " +
           "SUM(CASE WHEN a.attended = true THEN 1 ELSE 0 END) as classesAttended, " +
           "SUM(CASE WHEN a.attended = false THEN 1 ELSE 0 END) as classesMissed, " +
           "CAST(SUM(CASE WHEN a.attended = true THEN 1 ELSE 0 END) AS DOUBLE) / COUNT(a) * 100 as attendanceRate " +
           "FROM Student s " +
           "LEFT JOIN s.attendances a " +
           "WHERE a.attendanceDate BETWEEN :startDate AND :endDate " +
           "GROUP BY s.id, s.firstName, s.lastName, s.email " +
           "ORDER BY attendanceRate DESC")
    List<Map<String, Object>> getAttendanceReportByStudent(@Param("startDate") LocalDateTime startDate,
                                                          @Param("endDate") LocalDateTime endDate);

    /**
     * Estudiantes con baja asistencia (menos del porcentaje especificado)
     */
    @Query("SELECT s.id as studentId, " +
           "s.firstName as firstName, " +
           "s.lastName as lastName, " +
           "s.email as email, " +
           "CAST(SUM(CASE WHEN a.attended = true THEN 1 ELSE 0 END) AS DOUBLE) / COUNT(a) * 100 as attendanceRate " +
           "FROM Student s " +
           "JOIN s.attendances a " +
           "WHERE a.attendanceDate BETWEEN :startDate AND :endDate " +
           "GROUP BY s.id, s.firstName, s.lastName, s.email " +
           "HAVING (CAST(SUM(CASE WHEN a.attended = true THEN 1 ELSE 0 END) AS DOUBLE) / COUNT(a) * 100) < :minPercentage " +
           "ORDER BY (CAST(SUM(CASE WHEN a.attended = true THEN 1 ELSE 0 END) AS DOUBLE) / COUNT(a) * 100) ASC")
    List<Map<String, Object>> getStudentsWithLowAttendance(@Param("startDate") LocalDateTime startDate,
                                                          @Param("endDate") LocalDateTime endDate,
                                                          @Param("minPercentage") Double minPercentage);

    /**
     * Mejores estudiantes por asistencia
     */
    @Query("SELECT s.id as studentId, " +
           "s.firstName as firstName, " +
           "s.lastName as lastName, " +
           "s.email as email, " +
           "COUNT(a) as totalClasses, " +
           "SUM(CASE WHEN a.attended = true THEN 1 ELSE 0 END) as classesAttended, " +
           "CAST(SUM(CASE WHEN a.attended = true THEN 1 ELSE 0 END) AS DOUBLE) / COUNT(a) * 100 as attendanceRate " +
           "FROM Student s " +
           "JOIN s.attendances a " +
           "WHERE a.attendanceDate BETWEEN :startDate AND :endDate " +
           "GROUP BY s.id, s.firstName, s.lastName, s.email " +
           "HAVING COUNT(a) >= :minClasses AND (CAST(SUM(CASE WHEN a.attended = true THEN 1 ELSE 0 END) AS DOUBLE) / COUNT(a) * 100) >= :minPercentage " +
           "ORDER BY (CAST(SUM(CASE WHEN a.attended = true THEN 1 ELSE 0 END) AS DOUBLE) / COUNT(a) * 100) DESC, SUM(CASE WHEN a.attended = true THEN 1 ELSE 0 END) DESC")
    List<Map<String, Object>> getTopAttendanceStudents(@Param("startDate") LocalDateTime startDate,
                                                      @Param("endDate") LocalDateTime endDate,
                                                      @Param("minClasses") Long minClasses,
                                                      @Param("minPercentage") Double minPercentage);

    // ========== CONSULTAS POR PROFESOR ==========

    /**
     * Asistencias de las clases de un profesor específico
     */
    @Query("SELECT a FROM Attendance a WHERE a.classSession.teacher.id = :teacherId ORDER BY a.attendanceDate DESC")
    List<Attendance> findByTeacherId(@Param("teacherId") Long teacherId);

    /**
     * Estadísticas de asistencia para un profesor
     */
    @Query("SELECT " +
           "COUNT(a) as totalRecords, " +
           "SUM(CASE WHEN a.attended = true THEN 1 ELSE 0 END) as totalAttendances, " +
           "SUM(CASE WHEN a.attended = false THEN 1 ELSE 0 END) as totalAbsences, " +
           "CAST(SUM(CASE WHEN a.attended = true THEN 1 ELSE 0 END) AS DOUBLE) / COUNT(a) * 100 as attendancePercentage " +
           "FROM Attendance a " +
           "WHERE a.classSession.teacher.id = :teacherId " +
           "AND a.attendanceDate BETWEEN :startDate AND :endDate")
    Map<String, Object> getTeacherAttendanceStats(@Param("teacherId") Long teacherId,
                                                 @Param("startDate") LocalDateTime startDate,
                                                 @Param("endDate") LocalDateTime endDate);

    // ========== CONSULTAS DE VALIDACIÓN ==========

    /**
     * Verificar si existe registro de asistencia para estudiante y clase
     */
    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END " +
           "FROM Attendance a WHERE a.student.id = :studentId AND a.classSession.id = :classId")
    Boolean existsAttendanceRecord(@Param("studentId") Long studentId, @Param("classId") Long classId);

    /**
     * Obtener asistencias duplicadas (para limpieza de datos)
     */
    @Query("SELECT a.student.id, a.classSession.id, COUNT(a) as duplicateCount " +
           "FROM Attendance a " +
           "GROUP BY a.student.id, a.classSession.id " +
           "HAVING COUNT(a) > 1")
    List<Map<String, Object>> findDuplicateAttendanceRecords();

    // ========== CONSULTAS AVANZADAS ==========

    /**
     * Promedio de asistencia por día de la semana
     */
    @Query("SELECT DAYNAME(a.attendanceDate) as dayOfWeek, " +
           "COUNT(a) as totalClasses, " +
           "SUM(CASE WHEN a.attended = true THEN 1 ELSE 0 END) as attendances, " +
           "CAST(SUM(CASE WHEN a.attended = true THEN 1 ELSE 0 END) AS DOUBLE) / COUNT(a) * 100 as attendanceRate " +
           "FROM Attendance a " +
           "WHERE a.attendanceDate BETWEEN :startDate AND :endDate " +
           "GROUP BY DAYOFWEEK(a.attendanceDate), DAYNAME(a.attendanceDate) " +
           "ORDER BY DAYOFWEEK(a.attendanceDate)")
    List<Map<String, Object>> getAttendanceByDayOfWeek(@Param("startDate") LocalDateTime startDate,
                                                       @Param("endDate") LocalDateTime endDate);

    /**
     * Tendencia de asistencia por mes
     */
    @Query("SELECT YEAR(a.attendanceDate) as year, " +
           "MONTH(a.attendanceDate) as month, " +
           "MONTHNAME(a.attendanceDate) as monthName, " +
           "COUNT(a) as totalClasses, " +
           "SUM(CASE WHEN a.attended = true THEN 1 ELSE 0 END) as attendances, " +
           "CAST(SUM(CASE WHEN a.attended = true THEN 1 ELSE 0 END) AS DOUBLE) / COUNT(a) * 100 as attendanceRate " +
           "FROM Attendance a " +
           "WHERE a.attendanceDate BETWEEN :startDate AND :endDate " +
           "GROUP BY YEAR(a.attendanceDate), MONTH(a.attendanceDate) " +
           "ORDER BY year DESC, month DESC")
    List<Map<String, Object>> getAttendanceTrendByMonth(@Param("startDate") LocalDateTime startDate,
                                                       @Param("endDate") LocalDateTime endDate);

    /**
     * Ranking de cursos por asistencia
     */
    @Query("SELECT c.id as courseId, " +
           "c.title as courseTitle, " +
           "COUNT(a) as totalSessions, " +
           "SUM(CASE WHEN a.attended = true THEN 1 ELSE 0 END) as totalAttendances, " +
           "CAST(SUM(CASE WHEN a.attended = true THEN 1 ELSE 0 END) AS DOUBLE) / COUNT(a) * 100 as attendanceRate " +
           "FROM Attendance a " +
           "JOIN a.classSession.course c " +
           "WHERE a.attendanceDate BETWEEN :startDate AND :endDate " +
           "GROUP BY c.id, c.title " +
           "ORDER BY attendanceRate DESC, totalAttendances DESC")
    List<Map<String, Object>> getCourseAttendanceRanking(@Param("startDate") LocalDateTime startDate,
                                                        @Param("endDate") LocalDateTime endDate);
}