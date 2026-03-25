package com.edidev.academyApp.repository;

import com.edidev.academyApp.enums.EnrollmentStatus;
import com.edidev.academyApp.model.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    @Query("SELECT e FROM Enrollment e WHERE e.student.user.id = :userId ORDER BY e.createdAt DESC")
    List<Enrollment> findByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId);

    @Query("SELECT e FROM Enrollment e WHERE e.student.user.id = :userId")
    List<Enrollment> findByUserId(@Param("userId") Long userId);

    List<Enrollment> findByCourseIdOrderByCreatedAtDesc(Long courseId);

    @Query("SELECT e FROM Enrollment e WHERE e.student.user.id = :userId AND e.status = :status")
    List<Enrollment> findByUserIdAndStatus(@Param("userId") Long userId, @Param("status") EnrollmentStatus status);

    @Query("SELECT e FROM Enrollment e WHERE e.student.user.id = :userId AND e.course.id = :courseId AND e.status = :status")
    Optional<Enrollment> findByUserIdAndCourseIdAndStatus(@Param("userId") Long userId, @Param("courseId") Long courseId, @Param("status") EnrollmentStatus status);

    @Query("SELECT CASE WHEN COUNT(e) > 0 THEN true ELSE false END FROM Enrollment e WHERE e.student.user.id = :userId AND e.course.id = :courseId AND e.status = :status")
    boolean existsByUserIdAndCourseIdAndStatus(@Param("userId") Long userId, @Param("courseId") Long courseId, @Param("status") EnrollmentStatus status);

    @Query("SELECT e FROM Enrollment e WHERE e.status = 'ACTIVE' AND (e.purchasedHours - e.usedHours) > 0")
    List<Enrollment> findActiveEnrollmentsWithHours();

    @Query("SELECT e FROM Enrollment e WHERE e.status = 'ACTIVE' AND (e.purchasedHours - e.usedHours) <= :threshold")
    List<Enrollment> findActiveEnrollmentsWithLowHours(@Param("threshold") BigDecimal threshold);

    @Query("SELECT COUNT(e) FROM Enrollment e WHERE e.course.id = :courseId AND e.status = 'ACTIVE'")
    Long countActiveEnrollmentsByCourse(@Param("courseId") Long courseId);

    @Query("SELECT SUM(e.totalPaid) FROM Enrollment e WHERE e.course.id = :courseId")
    BigDecimal getTotalRevenueByCourse(@Param("courseId") Long courseId);

    @Query("SELECT e FROM Enrollment e WHERE e.student.user.id = :userId AND e.course.id = :courseId")
    List<Enrollment> findByUserAndCourse(@Param("userId") Long userId, @Param("courseId") Long courseId);

    // Métodos de conteo
    Long countByStatus(EnrollmentStatus status);
}