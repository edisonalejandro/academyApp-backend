package com.edidev.academyApp.repository;

import com.edidev.academyApp.model.ProductCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductCategoryRepository extends JpaRepository<ProductCategory, Long> {
    List<ProductCategory> findByIsActiveTrueOrderBySortOrderAsc();
    Optional<ProductCategory> findBySlug(String slug);
}
