package com.edidev.academyApp.repository;

import com.edidev.academyApp.model.Payment;
import com.edidev.academyApp.enums.StudentCategory;
import com.edidev.academyApp.enums.PricingType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByPaymentCode(String paymentCode);

    List<Payment> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<Payment> findByCourseIdOrderByCreatedAtDesc(Long courseId);

    @Query("SELECT SUM(p.finalPrice) FROM Payment p WHERE p.status = 'COMPLETED' AND p.paymentDate BETWEEN :startDate AND :endDate")
    Double getTotalRevenueBetweenDates(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(p) FROM Payment p WHERE p.status = 'COMPLETED' AND p.paymentDate BETWEEN :startDate AND :endDate")
    Long getCompletedPaymentsCountBetweenDates(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT p.studentCategory as category, SUM(p.finalPrice) as revenue FROM Payment p WHERE p.status = 'COMPLETED' AND p.paymentDate BETWEEN :startDate AND :endDate GROUP BY p.studentCategory")
    Map<StudentCategory, Double> getRevenueByStudentCategory(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT p.pricingType as pricingType, SUM(p.finalPrice) as revenue FROM Payment p WHERE p.status = 'COMPLETED' AND p.paymentDate BETWEEN :startDate AND :endDate GROUP BY p.pricingType")
    Map<PricingType, Double> getRevenueByPricingType(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT p.paymentMethod as method, SUM(p.finalPrice) as revenue FROM Payment p WHERE p.status = 'COMPLETED' AND p.paymentDate BETWEEN :startDate AND :endDate GROUP BY p.paymentMethod")
    Map<String, Double> getRevenueByPaymentMethod(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    List<Payment> findByStatusOrderByCreatedAtDesc(com.edidev.academyApp.enums.PaymentStatus status);

    @Query("SELECT p FROM Payment p WHERE p.user.email = :email ORDER BY p.createdAt DESC")
    List<Payment> findByUserEmailOrderByCreatedAtDesc(@Param("email") String email);

    // Métodos adicionales para Dashboard
    @Query("SELECT COALESCE(SUM(p.finalPrice), 0) FROM Payment p WHERE p.status = :status AND p.paymentDate BETWEEN :startDate AND :endDate")
    java.math.BigDecimal sumAmountByStatusAndDateBetween(@Param("status") com.edidev.academyApp.enums.PaymentStatus status, 
                                                          @Param("startDate") LocalDateTime startDate, 
                                                          @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(p) FROM Payment p WHERE p.status = :status AND p.paymentDate BETWEEN :startDate AND :endDate")
    Long countByStatusAndPaymentDateBetween(@Param("status") com.edidev.academyApp.enums.PaymentStatus status, 
                                             @Param("startDate") LocalDateTime startDate, 
                                             @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COALESCE(SUM(p.finalPrice), 0) FROM Payment p WHERE p.status = :status")
    java.math.BigDecimal sumAmountByStatus(@Param("status") com.edidev.academyApp.enums.PaymentStatus status);

    Long countByStatus(com.edidev.academyApp.enums.PaymentStatus status);
}