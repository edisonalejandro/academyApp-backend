package com.edidev.academyApp.repository;

import com.edidev.academyApp.model.ShoppingCart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ShoppingCartRepository extends JpaRepository<ShoppingCart, Long> {
    Optional<ShoppingCart> findByUserId(Long userId);
    Optional<ShoppingCart> findBySessionKey(String sessionKey);
}
