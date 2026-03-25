package com.edidev.academyApp.repository;

import com.edidev.academyApp.model.ClassSession;
import com.edidev.academyApp.enums.ClassStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ClassSessionRepository extends JpaRepository<ClassSession, Long> {

    List<ClassSession> findByCourseIdOrderByScheduledDateAsc(Long courseId);

    List<ClassSession> findByTeacherIdOrderByScheduledDateDesc(Long teacherId);

    List<ClassSession> findByStatusOrderByScheduledDateAsc(ClassStatus status);

    @Query("SELECT cs FROM ClassSession cs WHERE cs.course.id = :courseId AND cs.status = :status ORDER BY cs.scheduledDate ASC")
    List<ClassSession> findByCourseAndStatus(@Param("courseId") Long courseId, @Param("status") ClassStatus status);

    @Query("SELECT cs FROM ClassSession cs WHERE cs.scheduledDate BETWEEN :startDate AND :endDate ORDER BY cs.scheduledDate ASC")
    List<ClassSession> findByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT cs FROM ClassSession cs WHERE cs.teacher.id = :teacherId AND cs.scheduledDate BETWEEN :startDate AND :endDate ORDER BY cs.scheduledDate ASC")
    List<ClassSession> findByTeacherAndDateRange(@Param("teacherId") Long teacherId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT cs FROM ClassSession cs WHERE cs.status = 'SCHEDULED' AND cs.scheduledDate < :now")
    List<ClassSession> findOverdueScheduledSessions(@Param("now") LocalDateTime now);

    // Métodos adicionales para Dashboard
    @Query("SELECT COUNT(cs) FROM ClassSession cs WHERE cs.status = :status AND cs.scheduledDate BETWEEN :startDate AND :endDate")
    Long countByStatusAndScheduledDateBetween(@Param("status") ClassStatus status, 
                                               @Param("startDate") LocalDateTime startDate, 
                                               @Param("endDate") LocalDateTime endDate);

    @Query("SELECT cs FROM ClassSession cs WHERE cs.status IN ('SCHEDULED', 'IN_PROGRESS') AND cs.scheduledDate BETWEEN :startDate AND :endDate ORDER BY cs.scheduledDate ASC")
    List<ClassSession> findUpcomingSessionsByDateRange(@Param("startDate") LocalDateTime startDate, 
                                                        @Param("endDate") LocalDateTime endDate);
}