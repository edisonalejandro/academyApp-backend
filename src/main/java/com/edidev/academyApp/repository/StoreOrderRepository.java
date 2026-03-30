package com.edidev.academyApp.repository;

import com.edidev.academyApp.enums.OrderStatus;
import com.edidev.academyApp.model.StoreOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StoreOrderRepository extends JpaRepository<StoreOrder, Long> {
    List<StoreOrder> findByUserIdOrderByCreatedAtDesc(Long userId);
    Page<StoreOrder> findAllByOrderByCreatedAtDesc(Pageable pageable);
    Optional<StoreOrder> findByOrderNumber(String orderNumber);
    List<StoreOrder> findByStatus(OrderStatus status);
}
