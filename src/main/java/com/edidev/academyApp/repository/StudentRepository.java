package com.edidev.academyApp.repository;

import com.edidev.academyApp.enums.StudentCategory;
import com.edidev.academyApp.enums.StudentStatus;
import com.edidev.academyApp.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {

    Optional<Student> findByUserId(Long userId);

    Optional<Student> findByEmail(String email);

    boolean existsByEmail(String email);

    List<Student> findByStatusOrderByLastNameAsc(StudentStatus status);

    List<Student> findByCategoryOrderByLastNameAsc(StudentCategory category);

    @Query("SELECT s FROM Student s WHERE " +
           "LOWER(s.firstName) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
           "LOWER(s.lastName) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
           "LOWER(s.email) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
           "LOWER(s.phone) LIKE LOWER(CONCAT('%', :term, '%'))")
    List<Student> searchByTerm(@Param("term") String term);

    List<Student> findAllByOrderByLastNameAscFirstNameAsc();

    // Métodos de conteo
    Long countByStatus(StudentStatus status);

    @Query("SELECT COUNT(s) FROM Student s WHERE s.createdAt BETWEEN :startDate AND :endDate")
    Long countByCreatedAtBetween(@Param("startDate") java.time.LocalDateTime startDate, 
                                  @Param("endDate") java.time.LocalDateTime endDate);
}
