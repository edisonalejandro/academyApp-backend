package com.edidev.academyApp.repository;

import com.edidev.academyApp.model.Course;
import com.edidev.academyApp.enums.DanceType;
import com.edidev.academyApp.enums.DanceLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {

    Optional<Course> findByCode(String code);

    List<Course> findByIsActiveTrueOrderByTitleAsc();

    List<Course> findByDanceTypeAndIsActiveTrueOrderByLevelAsc(DanceType danceType);

    List<Course> findByLevelAndIsActiveTrueOrderByTitleAsc(DanceLevel level);

    List<Course> findByTeacherIdAndIsActiveTrueOrderByTitleAsc(Long teacherId);

    @Query("SELECT c FROM Course c WHERE c.isActive = true AND " +
           "(LOWER(c.title) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(c.description) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(c.code) LIKE LOWER(CONCAT('%', :search, '%')))")
    List<Course> searchActiveCoursesIgnoreCase(@Param("search") String search);

    @Query("SELECT c FROM Course c WHERE c.isActive = true AND " +
           "c.danceType = :danceType AND c.level = :level")
    List<Course> findByDanceTypeAndLevel(@Param("danceType") DanceType danceType, 
                                        @Param("level") DanceLevel level);

    @Query("SELECT COUNT(e) FROM Course c JOIN c.enrollments e WHERE c.id = :courseId AND e.status = 'ACTIVE'")
    Long countActiveEnrollmentsByCourseId(@Param("courseId") Long courseId);

    boolean existsByCodeAndIsActiveTrue(String code);

    @Query("SELECT DISTINCT c.danceType FROM Course c WHERE c.isActive = true ORDER BY c.danceType")
    List<DanceType> findDistinctActiveDanceTypes();

    @Query("SELECT DISTINCT c.level FROM Course c WHERE c.isActive = true ORDER BY c.level")
    List<DanceLevel> findDistinctActiveLevels();
}