package com.edidev.academyApp.repository;

import com.edidev.academyApp.model.PricingRule;
import com.edidev.academyApp.enums.PricingType;
import com.edidev.academyApp.enums.StudentCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PricingRuleRepository extends JpaRepository<PricingRule, Long> {

    List<PricingRule> findByStudentCategoryAndPersonCountAndIsActiveTrueOrderByClassQuantityAsc(
            StudentCategory studentCategory, Integer personCount);

    Optional<PricingRule> findByPricingTypeAndStudentCategoryAndPersonCountAndIsActiveTrue(
            PricingType pricingType, StudentCategory studentCategory, Integer personCount);

    boolean existsByPricingTypeAndStudentCategoryAndPersonCountAndIsActiveTrue(
            PricingType pricingType, StudentCategory studentCategory, Integer personCount);

    @Query("SELECT pr FROM PricingRule pr WHERE " +
           "(:pricingType IS NULL OR pr.pricingType = :pricingType) AND " +
           "(:studentCategory IS NULL OR pr.studentCategory = :studentCategory) AND " +
           "(:isActive IS NULL OR pr.isActive = :isActive) " +
           "ORDER BY pr.createdAt DESC")
    List<PricingRule> findByCriteria(@Param("pricingType") PricingType pricingType,
                                   @Param("studentCategory") StudentCategory studentCategory,
                                   @Param("isActive") Boolean isActive);

    List<PricingRule> findByIsActiveTrueOrderByStudentCategoryAscPersonCountAscClassQuantityAsc();
}