package com.edidev.academyApp.repository;

import com.edidev.academyApp.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByIsActiveTrueOrderByFeaturedDescNameAsc();
    List<Product> findByCategoryIdAndIsActiveTrueOrderByNameAsc(Long categoryId);
    List<Product> findByFeaturedTrueAndIsActiveTrueOrderByNameAsc();
    Optional<Product> findBySlug(String slug);

    @Query("SELECT p FROM Product p WHERE p.isActive = true AND " +
           "(LOWER(p.name) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :term, '%')))")
    List<Product> searchActiveProducts(@Param("term") String term);
}
