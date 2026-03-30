package com.edidev.academyApp.service;

import com.edidev.academyApp.dto.CheckoutRequest;
import com.edidev.academyApp.dto.StoreOrderDTO;
import com.edidev.academyApp.dto.StoreOrderItemDTO;
import com.edidev.academyApp.enums.OrderStatus;
import com.edidev.academyApp.exception.ResourceNotFoundException;
import com.edidev.academyApp.model.*;
import com.edidev.academyApp.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class StoreOrderService {

    private final StoreOrderRepository orderRepository;
    private final ShoppingCartRepository cartRepository;
    private final ProductVariantRepository variantRepository;
    private final UserRepository userRepository;

    private static final AtomicLong SEQ = new AtomicLong(System.currentTimeMillis() % 100000);

    // ===================== CHECKOUT =====================

    public StoreOrderDTO checkout(Long userId, CheckoutRequest request) {
        ShoppingCart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Carrito vacío o no encontrado"));

        if (cart.getItems().isEmpty()) {
            throw new IllegalStateException("El carrito está vacío");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        StoreOrder order = buildOrder(cart, request, user);
        StoreOrder saved = orderRepository.save(order);

        // Decrementar stock
        decrementStock(cart);

        // Limpiar carrito
        cart.getItems().clear();
        cartRepository.save(cart);

        log.info("Orden creada: {} para usuario {}", saved.getOrderNumber(), userId);
        return toDTO(saved);
    }

    public StoreOrderDTO checkoutByEmail(String email, CheckoutRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + email));
        return checkout(user.getId(), request);
    }

    public List<StoreOrderDTO> getMyOrdersByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + email));
        return getMyOrders(user.getId());
    }

    // ===================== CONSULTAS =====================

    @Transactional(readOnly = true)
    public List<StoreOrderDTO> getMyOrders(Long userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public StoreOrderDTO getOrderById(Long id) {
        return toDTO(findById(id));
    }

    @Transactional(readOnly = true)
    public StoreOrderDTO getOrderByNumber(String orderNumber) {
        return toDTO(orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Orden no encontrada: " + orderNumber)));
    }

    @Transactional(readOnly = true)
    public Page<StoreOrderDTO> getAllOrders(Pageable pageable) {
        return orderRepository.findAllByOrderByCreatedAtDesc(pageable).map(this::toDTO);
    }

    // ===================== GESTIÓN ADMIN =====================

    public StoreOrderDTO updateStatus(Long id, OrderStatus newStatus) {
        StoreOrder order = findById(id);
        order.setStatus(newStatus);
        if (newStatus == OrderStatus.PAID) order.setPaidAt(LocalDateTime.now());
        if (newStatus == OrderStatus.SHIPPED) order.setShippedAt(LocalDateTime.now());
        if (newStatus == OrderStatus.DELIVERED) order.setDeliveredAt(LocalDateTime.now());
        return toDTO(orderRepository.save(order));
    }

    // ===================== HELPERS =====================

    private StoreOrder buildOrder(ShoppingCart cart, CheckoutRequest req, User user) {
        BigDecimal subtotal = cart.getItems().stream()
                .map(item -> {
                    BigDecimal unit = item.getVariant().getProduct().getBasePrice()
                            .add(item.getVariant().getAdditionalPrice());
                    return unit.multiply(BigDecimal.valueOf(item.getQuantity()));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        String orderNumber = "ORD-" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) +
                "-" + String.format("%05d", SEQ.incrementAndGet());

        StoreOrder order = StoreOrder.builder()
                .orderNumber(orderNumber)
                .user(user)
                .customerName(req.getCustomerName())
                .customerEmail(req.getCustomerEmail())
                .customerPhone(req.getCustomerPhone())
                .paymentMethod(req.getPaymentMethod())
                .subtotal(subtotal)
                .discountAmount(BigDecimal.ZERO)
                .totalAmount(subtotal)
                .notes(req.getNotes())
                .shippingAddress(req.getShippingAddress())
                .status(OrderStatus.CONFIRMED)
                .build();

        List<StoreOrderItem> orderItems = cart.getItems().stream().map(cartItem -> {
            ProductVariant variant = cartItem.getVariant();
            BigDecimal unit = variant.getProduct().getBasePrice().add(variant.getAdditionalPrice());
            return StoreOrderItem.builder()
                    .order(order)
                    .variant(variant)
                    .productName(variant.getProduct().getName())
                    .variantSize(variant.getSize().name())
                    .variantColor(variant.getColor())
                    .quantity(cartItem.getQuantity())
                    .unitPrice(unit)
                    .subtotal(unit.multiply(BigDecimal.valueOf(cartItem.getQuantity())))
                    .build();
        }).collect(Collectors.toList());

        order.setItems(orderItems);
        return order;
    }

    private void decrementStock(ShoppingCart cart) {
        cart.getItems().forEach(item -> {
            ProductVariant variant = item.getVariant();
            variant.setStock(variant.getStock() - item.getQuantity());
            variantRepository.save(variant);
        });
    }

    private StoreOrder findById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Orden no encontrada: " + id));
    }

    private StoreOrderDTO toDTO(StoreOrder o) {
        return StoreOrderDTO.builder()
                .id(o.getId())
                .orderNumber(o.getOrderNumber())
                .userId(o.getUser() != null ? o.getUser().getId() : null)
                .customerName(o.getCustomerName())
                .customerEmail(o.getCustomerEmail())
                .customerPhone(o.getCustomerPhone())
                .status(o.getStatus())
                .paymentMethod(o.getPaymentMethod())
                .subtotal(o.getSubtotal())
                .discountAmount(o.getDiscountAmount())
                .totalAmount(o.getTotalAmount())
                .notes(o.getNotes())
                .shippingAddress(o.getShippingAddress())
                .items(o.getItems().stream().map(this::toItemDTO).collect(Collectors.toList()))
                .paidAt(o.getPaidAt())
                .shippedAt(o.getShippedAt())
                .deliveredAt(o.getDeliveredAt())
                .createdAt(o.getCreatedAt())
                .updatedAt(o.getUpdatedAt())
                .build();
    }

    private StoreOrderItemDTO toItemDTO(StoreOrderItem i) {
        return StoreOrderItemDTO.builder()
                .id(i.getId())
                .variantId(i.getVariant() != null ? i.getVariant().getId() : null)
                .productName(i.getProductName())
                .variantSize(i.getVariantSize())
                .variantColor(i.getVariantColor())
                .quantity(i.getQuantity())
                .unitPrice(i.getUnitPrice())
                .subtotal(i.getSubtotal())
                .build();
    }
}
