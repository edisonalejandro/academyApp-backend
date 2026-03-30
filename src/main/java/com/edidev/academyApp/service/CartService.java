package com.edidev.academyApp.service;

import com.edidev.academyApp.dto.AddToCartRequest;
import com.edidev.academyApp.dto.CartDTO;
import com.edidev.academyApp.dto.CartItemDTO;
import com.edidev.academyApp.exception.ResourceNotFoundException;
import com.edidev.academyApp.model.*;
import com.edidev.academyApp.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CartService {

    private final ShoppingCartRepository cartRepository;
    private final ProductVariantRepository variantRepository;
    private final UserRepository userRepository;

    public CartDTO getCartForUser(Long userId) {
        ShoppingCart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> createCartForUser(userId));
        return toDTO(cart);
    }

    public CartDTO getCartBySession(String sessionKey) {
        ShoppingCart cart = cartRepository.findBySessionKey(sessionKey)
                .orElseGet(() -> createCartForSession(sessionKey));
        return toDTO(cart);
    }

    public CartDTO addItem(Long userId, AddToCartRequest request) {
        ShoppingCart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> createCartForUser(userId));
        return addItemToCart(cart, request);
    }

    public CartDTO addItemBySession(String sessionKey, AddToCartRequest request) {
        ShoppingCart cart = cartRepository.findBySessionKey(sessionKey)
                .orElseGet(() -> createCartForSession(sessionKey));
        return addItemToCart(cart, request);
    }

    public CartDTO updateItemQuantity(Long userId, Long variantId, Integer quantity) {
        ShoppingCart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Carrito no encontrado"));

        Optional<CartItem> itemOpt = cart.getItems().stream()
                .filter(i -> i.getVariant().getId().equals(variantId))
                .findFirst();

        if (itemOpt.isEmpty()) {
            throw new ResourceNotFoundException("Ítem no encontrado en el carrito");
        }

        CartItem item = itemOpt.get();
        if (quantity <= 0) {
            cart.getItems().remove(item);
        } else {
            item.setQuantity(quantity);
        }
        return toDTO(cartRepository.save(cart));
    }

    public CartDTO removeItem(Long userId, Long variantId) {
        ShoppingCart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Carrito no encontrado"));
        cart.getItems().removeIf(i -> i.getVariant().getId().equals(variantId));
        return toDTO(cartRepository.save(cart));
    }

    public void clearCart(Long userId) {
        cartRepository.findByUserId(userId).ifPresent(cart -> {
            cart.getItems().clear();
            cartRepository.save(cart);
        });
    }

    // ===================== HELPERS PRIVADOS =====================

    private CartDTO addItemToCart(ShoppingCart cart, AddToCartRequest request) {
        ProductVariant variant = variantRepository.findById(request.getVariantId())
                .orElseThrow(() -> new ResourceNotFoundException("Variante no encontrada: " + request.getVariantId()));

        if (variant.getStock() < request.getQuantity()) {
            throw new IllegalStateException("Stock insuficiente. Disponible: " + variant.getStock());
        }

        Optional<CartItem> existing = cart.getItems().stream()
                .filter(i -> i.getVariant().getId().equals(variant.getId()))
                .findFirst();

        if (existing.isPresent()) {
            int newQty = existing.get().getQuantity() + request.getQuantity();
            if (variant.getStock() < newQty) {
                throw new IllegalStateException("Stock insuficiente. Disponible: " + variant.getStock());
            }
            existing.get().setQuantity(newQty);
        } else {
            CartItem newItem = CartItem.builder()
                    .cart(cart)
                    .variant(variant)
                    .quantity(request.getQuantity())
                    .build();
            cart.getItems().add(newItem);
        }

        return toDTO(cartRepository.save(cart));
    }

    private ShoppingCart createCartForUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + userId));
        return cartRepository.save(ShoppingCart.builder().user(user).build());
    }

    private ShoppingCart createCartForSession(String sessionKey) {
        return cartRepository.save(ShoppingCart.builder().sessionKey(sessionKey).build());
    }

    private CartDTO toDTO(ShoppingCart cart) {
        List<CartItemDTO> items = cart.getItems().stream()
                .map(this::toItemDTO)
                .collect(Collectors.toList());

        BigDecimal subtotal = items.stream()
                .map(CartItemDTO::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int totalItems = items.stream()
                .mapToInt(CartItemDTO::getQuantity)
                .sum();

        return CartDTO.builder()
                .id(cart.getId())
                .userId(cart.getUser() != null ? cart.getUser().getId() : null)
                .items(items)
                .totalItems(totalItems)
                .subtotal(subtotal)
                .updatedAt(cart.getUpdatedAt())
                .build();
    }

    private CartItemDTO toItemDTO(CartItem item) {
        ProductVariant variant = item.getVariant();
        Product product = variant.getProduct();
        BigDecimal unitPrice = product.getBasePrice().add(variant.getAdditionalPrice());
        BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(item.getQuantity()));

        return CartItemDTO.builder()
                .id(item.getId())
                .variantId(variant.getId())
                .productId(product.getId())
                .productName(product.getName())
                .productImageUrl(product.getImageUrl())
                .categoryName(product.getCategory().getName())
                .size(variant.getSize())
                .color(variant.getColor())
                .quantity(item.getQuantity())
                .unitPrice(unitPrice)
                .subtotal(subtotal)
                .availableStock(variant.getStock())
                .addedAt(item.getAddedAt())
                .build();
    }

    // ===================== MÉTODOS POR EMAIL =====================

    private Long resolveUserId(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + email))
                .getId();
    }

    public CartDTO getCartForEmail(String email) {
        return getCartForUser(resolveUserId(email));
    }

    public CartDTO addItemByEmail(String email, AddToCartRequest request) {
        return addItem(resolveUserId(email), request);
    }

    public CartDTO updateItemQuantityByEmail(String email, Long variantId, Integer quantity) {
        return updateItemQuantity(resolveUserId(email), variantId, quantity);
    }

    public CartDTO removeItemByEmail(String email, Long variantId) {
        return removeItem(resolveUserId(email), variantId);
    }

    public void clearCartByEmail(String email) {
        clearCart(resolveUserId(email));
    }
}
