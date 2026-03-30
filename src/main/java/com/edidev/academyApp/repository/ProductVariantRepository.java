package com.edidev.academyApp.repository;

import com.edidev.academyApp.model.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {
    List<ProductVariant> findByProductIdAndIsActiveTrueOrderBySizeAsc(Long productId);
}
