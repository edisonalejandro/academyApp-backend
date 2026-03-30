package com.edidev.academyApp.controller;

import com.edidev.academyApp.dto.CheckoutRequest;
import com.edidev.academyApp.dto.StoreOrderDTO;
import com.edidev.academyApp.enums.OrderStatus;
import com.edidev.academyApp.service.StoreOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/catalog/orders")
@RequiredArgsConstructor
@Tag(name = "StoreOrders", description = "APIs de órdenes de la tienda")
public class StoreOrderController {

    private final StoreOrderService orderService;

    @PostMapping("/checkout")
    @Operation(summary = "Confirmar compra y crear orden")
    public ResponseEntity<StoreOrderDTO> checkout(Authentication authentication,
                                                   @Valid @RequestBody CheckoutRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(orderService.checkoutByEmail(authentication.getName(), request));
    }

    @GetMapping("/my-orders")
    @Operation(summary = "Mis órdenes")
    public ResponseEntity<List<StoreOrderDTO>> getMyOrders(Authentication authentication) {
        return ResponseEntity.ok(orderService.getMyOrdersByEmail(authentication.getName()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener orden por ID")
    public ResponseEntity<StoreOrderDTO> getOrderById(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getOrderById(id));
    }

    @GetMapping("/number/{orderNumber}")
    @Operation(summary = "Obtener orden por número")
    public ResponseEntity<StoreOrderDTO> getOrderByNumber(@PathVariable String orderNumber) {
        return ResponseEntity.ok(orderService.getOrderByNumber(orderNumber));
    }

    // ===== ADMIN =====

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Listar todas las órdenes (Admin)")
    public ResponseEntity<Page<StoreOrderDTO>> getAllOrders(
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(orderService.getAllOrders(pageable));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Actualizar estado de orden (Admin)")
    public ResponseEntity<StoreOrderDTO> updateStatus(@PathVariable Long id,
                                                       @RequestBody Map<String, String> body) {
        OrderStatus status = OrderStatus.valueOf(body.get("status"));
        return ResponseEntity.ok(orderService.updateStatus(id, status));
    }
}
