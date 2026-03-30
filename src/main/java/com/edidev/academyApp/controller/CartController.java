package com.edidev.academyApp.controller;

import com.edidev.academyApp.dto.AddToCartRequest;
import com.edidev.academyApp.dto.CartDTO;
import com.edidev.academyApp.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/catalog/cart")
@RequiredArgsConstructor
@Tag(name = "Cart", description = "APIs del carrito de compras")
public class CartController {

    private final CartService cartService;

    @GetMapping
    @Operation(summary = "Obtener carrito del usuario autenticado")
    public ResponseEntity<CartDTO> getCart(Authentication authentication,
                                            @RequestHeader(value = "X-Session-Key", required = false) String sessionKey) {
        if (authentication != null && authentication.isAuthenticated()) {
            return ResponseEntity.ok(cartService.getCartForEmail(authentication.getName()));
        }
        if (sessionKey != null) {
            return ResponseEntity.ok(cartService.getCartBySession(sessionKey));
        }
        return ResponseEntity.ok(CartDTO.builder().build());
    }

    @PostMapping("/items")
    @Operation(summary = "Agregar ítem al carrito")
    public ResponseEntity<CartDTO> addItem(Authentication authentication,
                                            @RequestHeader(value = "X-Session-Key", required = false) String sessionKey,
                                            @Valid @RequestBody AddToCartRequest request) {
        if (authentication != null && authentication.isAuthenticated()) {
            return ResponseEntity.ok(cartService.addItemByEmail(authentication.getName(), request));
        }
        if (sessionKey != null) {
            return ResponseEntity.ok(cartService.addItemBySession(sessionKey, request));
        }
        return ResponseEntity.badRequest().build();
    }

    @PutMapping("/items/{variantId}")
    @Operation(summary = "Actualizar cantidad de un ítem")
    public ResponseEntity<CartDTO> updateItem(Authentication authentication,
                                               @PathVariable Long variantId,
                                               @RequestBody Map<String, Integer> body) {
        Integer quantity = body.get("quantity");
        if (quantity == null) return ResponseEntity.badRequest().build();
        return ResponseEntity.ok(cartService.updateItemQuantityByEmail(authentication.getName(), variantId, quantity));
    }

    @DeleteMapping("/items/{variantId}")
    @Operation(summary = "Eliminar ítem del carrito")
    public ResponseEntity<CartDTO> removeItem(Authentication authentication,
                                               @PathVariable Long variantId) {
        return ResponseEntity.ok(cartService.removeItemByEmail(authentication.getName(), variantId));
    }

    @DeleteMapping
    @Operation(summary = "Vaciar carrito")
    public ResponseEntity<Void> clearCart(Authentication authentication) {
        cartService.clearCartByEmail(authentication.getName());
        return ResponseEntity.noContent().build();
    }
}
