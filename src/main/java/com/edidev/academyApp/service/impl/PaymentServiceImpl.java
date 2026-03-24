package com.edidev.academyApp.service.impl;

import com.edidev.academyApp.dto.PaymentRequestDTO;
import com.edidev.academyApp.dto.PaymentResponseDTO;
import com.edidev.academyApp.enums.PaymentStatus;
import com.edidev.academyApp.enums.StudentCategory;
import com.edidev.academyApp.enums.PricingType;
import com.edidev.academyApp.exception.PaymentNotFoundException;
import com.edidev.academyApp.exception.UserNotFoundException;
import com.edidev.academyApp.model.*;
import com.edidev.academyApp.repository.*;
import com.edidev.academyApp.service.EnrollmentService;
import com.edidev.academyApp.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final PricingRuleRepository pricingRuleRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentService enrollmentService;

    /**
     * Calcula pago usando el sistema flexible de reglas de precios
     */
    @Override
    public BigDecimal calculatePaymentFlexible(int numberOfClasses, StudentCategory studentCategory, boolean isCouple) {
        Integer personCount = isCouple ? 2 : 1;
        
        // Buscar regla de precios correspondiente
        PricingType pricingType = mapClassesToPricingType(numberOfClasses, isCouple);
        
        return pricingRuleRepository.findByPricingTypeAndStudentCategoryAndPersonCountAndIsActiveTrue(
                pricingType, studentCategory, personCount)
                .map(PricingRule::getFinalPrice)
                .orElseThrow(() -> new RuntimeException(
                    String.format("No se encontró regla de precios para: %d clases, categoría: %s, personas: %d", 
                            numberOfClasses, studentCategory, personCount)));
    }

    /**
     * Calcula pago usando regla de precios específica
     */
    public BigDecimal calculatePaymentByRule(Long pricingRuleId) {
        PricingRule pricingRule = pricingRuleRepository.findById(pricingRuleId)
                .orElseThrow(() -> new RuntimeException("Regla de precios no encontrada: " + pricingRuleId));
        
        if (!pricingRule.getIsActive()) {
            throw new RuntimeException("La regla de precios no está activa: " + pricingRuleId);
        }
        
        return pricingRule.getFinalPrice();
    }

    /**
     * Procesa un nuevo pago completo
     */
    @Override
    public PaymentResponseDTO processPayment(PaymentRequestDTO request, String userEmail) {
        log.info("🔄 Procesando pago para usuario: {}", userEmail);

        // Validar usuario
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado: " + userEmail));

        // Validar curso
        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new RuntimeException("Curso no encontrado: " + request.getCourseId()));

        // Validar regla de precios
        PricingRule pricingRule = pricingRuleRepository.findById(request.getPricingRuleId())
                .orElseThrow(() -> new RuntimeException("Regla de precios no encontrada: " + request.getPricingRuleId()));

        // Validar que la regla coincida con los parámetros
        validatePricingRule(pricingRule, request);

        // Crear el pago
        Payment payment = createPayment(user, course, pricingRule, request);
        
        // Procesar el pago según el método
        processPaymentMethod(payment, request);

        // Guardar el pago
        Payment savedPayment = paymentRepository.save(payment);

        // Si el pago es exitoso, crear/actualizar enrollment
        if (payment.getStatus() == PaymentStatus.COMPLETED) {
            enrollmentService.createOrUpdateEnrollment(user, course, payment);
        }

        log.info("✅ Pago procesado exitosamente - Código: {}", savedPayment.getPaymentCode());
        return mapToPaymentResponseDTO(savedPayment);
    }

    /**
     * Obtiene un pago por código
     */
    @Override
    public PaymentResponseDTO getPaymentByCode(String paymentCode) {
        Payment payment = paymentRepository.findByPaymentCode(paymentCode)
                .orElseThrow(() -> new PaymentNotFoundException("Pago no encontrado: " + paymentCode));
        
        return mapToPaymentResponseDTO(payment);
    }

    /**
     * Obtiene los pagos de un usuario
     */
    @Override
    public List<PaymentResponseDTO> getPaymentsByUser(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado: " + userEmail));

        return paymentRepository.findByUserIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(this::mapToPaymentResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene los pagos de un curso
     */
    @Override
    public List<PaymentResponseDTO> getPaymentsByCourse(Long courseId) {
        return paymentRepository.findByCourseIdOrderByCreatedAtDesc(courseId)
                .stream()
                .map(this::mapToPaymentResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Actualiza el estado de un pago
     */
    @Override
    public PaymentResponseDTO updatePaymentStatus(Long paymentId, PaymentStatus status, String notes) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException("Pago no encontrado: " + paymentId));

        PaymentStatus oldStatus = payment.getStatus();
        payment.setStatus(status);
        payment.setNotes(notes);

        if (status == PaymentStatus.COMPLETED && oldStatus != PaymentStatus.COMPLETED) {
            payment.setPaymentDate(LocalDateTime.now());
            // Crear/actualizar enrollment
            enrollmentService.createOrUpdateEnrollment(payment.getUser(), payment.getCourse(), payment);
        }

        Payment savedPayment = paymentRepository.save(payment);
        log.info("📊 Estado de pago actualizado - Código: {}, Estado: {}", payment.getPaymentCode(), status);
        
        return mapToPaymentResponseDTO(savedPayment);
    }

    /**
     * Genera reporte de ingresos
     */
    @Override
    public Map<String, Object> getRevenueReport(String startDateStr, String endDateStr) {
        LocalDateTime startDate = LocalDateTime.parse(startDateStr + "T00:00:00");
        LocalDateTime endDate = LocalDateTime.parse(endDateStr + "T23:59:59");

        Double totalRevenue = paymentRepository.getTotalRevenueBetweenDates(startDate, endDate);
        Long totalPayments = paymentRepository.getCompletedPaymentsCountBetweenDates(startDate, endDate);

        Map<String, Object> report = new HashMap<>();
        report.put("startDate", startDateStr);
        report.put("endDate", endDateStr);
        report.put("totalRevenue", totalRevenue != null ? totalRevenue : 0.0);
        report.put("totalPayments", totalPayments);
        report.put("averagePayment", totalPayments > 0 ? totalRevenue / totalPayments : 0.0);

        // Agregar estadísticas adicionales
        Map<String, Object> breakdown = getRevenueBreakdown(startDate, endDate);
        report.put("breakdown", breakdown);

        log.info("📈 Reporte generado: ${} CLP en {} pagos", totalRevenue, totalPayments);
        return report;
    }

    /**
     * Obtiene desglose detallado de ingresos
     */
    public Map<String, Object> getRevenueBreakdown(LocalDateTime startDate, LocalDateTime endDate) {
        Map<String, Object> breakdown = new HashMap<>();
        
        // Ingresos por categoría de estudiante
        Map<StudentCategory, Double> revenueByCategory = paymentRepository
                .getRevenueByStudentCategory(startDate, endDate);
        breakdown.put("byStudentCategory", revenueByCategory);
        
        // Ingresos por tipo de paquete
        Map<PricingType, Double> revenueByPricingType = paymentRepository
                .getRevenueByPricingType(startDate, endDate);
        breakdown.put("byPricingType", revenueByPricingType);
        
        // Ingresos por método de pago
        Map<String, Double> revenueByPaymentMethod = paymentRepository
                .getRevenueByPaymentMethod(startDate, endDate);
        breakdown.put("byPaymentMethod", revenueByPaymentMethod);
        
        return breakdown;
    }

    /**
     * Valida disponibilidad de una regla de precios
     */
    public boolean isPricingRuleAvailable(Long pricingRuleId, Long courseId, Integer personCount) {
        PricingRule rule = pricingRuleRepository.findById(pricingRuleId)
                .orElse(null);
        
        if (rule == null || !rule.getIsActive()) {
            return false;
        }
        
        // Validar que el número de personas coincida
        if (!rule.getPersonCount().equals(personCount)) {
            return false;
        }
        
        // Validar fechas de vigencia si están definidas
        LocalDateTime now = LocalDateTime.now();
        if (rule.getValidFrom() != null && now.isBefore(rule.getValidFrom())) {
            return false;
        }
        
        if (rule.getValidUntil() != null && now.isAfter(rule.getValidUntil())) {
            return false;
        }
        
        return true;
    }

    // MÉTODOS PRIVADOS

    private PricingType mapClassesToPricingType(int numberOfClasses, boolean isCouple) {
        if (isCouple && numberOfClasses == 8) {
            return PricingType.COUPLE_PACKAGE_8;
        }
        
        return switch (numberOfClasses) {
            case 1 -> PricingType.SINGLE_CLASS;
            case 4 -> PricingType.PACKAGE_4;
            case 8 -> PricingType.PACKAGE_8;
            case 12 -> PricingType.PACKAGE_12;
            default -> throw new IllegalArgumentException("Número de clases no soportado: " + numberOfClasses);
        };
    }

    private void validatePricingRule(PricingRule rule, PaymentRequestDTO request) {
        if (!rule.getIsActive()) {
            throw new RuntimeException("La regla de precios no está activa");
        }
        
        if (!rule.getStudentCategory().equals(request.getStudentCategory())) {
            throw new RuntimeException("La categoría de estudiante no coincide con la regla de precios");
        }
        
        if (!rule.getPersonCount().equals(request.getPersonCount())) {
            throw new RuntimeException("El número de personas no coincide con la regla de precios");
        }
        
        // Validar fechas de vigencia
        LocalDateTime now = LocalDateTime.now();
        if (rule.getValidFrom() != null && now.isBefore(rule.getValidFrom())) {
            throw new RuntimeException("La regla de precios aún no está vigente");
        }
        
        if (rule.getValidUntil() != null && now.isAfter(rule.getValidUntil())) {
            throw new RuntimeException("La regla de precios ha expirado");
        }
    }

    private Payment createPayment(User user, Course course, PricingRule pricingRule, PaymentRequestDTO request) {
        BigDecimal finalPrice = pricingRule.getFinalPrice();
        BigDecimal discountAmount = pricingRule.getPrice().subtract(finalPrice);

        return Payment.builder()
                .user(user)
                .course(course)
                .pricingRule(pricingRule)
                .pricingType(pricingRule.getPricingType())
                .studentCategory(request.getStudentCategory())
                .quantityClasses(pricingRule.getClassQuantity())
                .personCount(request.getPersonCount())
                .originalPrice(pricingRule.getPrice())
                .discountAmount(discountAmount)
                .finalPrice(finalPrice)
                .paymentMethod(request.getPaymentMethod())
                .transactionId(request.getTransactionId())
                .notes(request.getNotes())
                .status(PaymentStatus.PENDING)
                .build();
    }

    private void processPaymentMethod(Payment payment, PaymentRequestDTO request) {
        switch (request.getPaymentMethod()) {
            case CASH -> {
                payment.setStatus(PaymentStatus.COMPLETED);
                payment.setPaymentDate(LocalDateTime.now());
                log.info("💵 Pago en efectivo completado inmediatamente");
            }
            case CREDIT_CARD, DEBIT_CARD -> {
                if (request.getTransactionId() != null && !request.getTransactionId().isEmpty()) {
                    payment.setStatus(PaymentStatus.COMPLETED);
                    payment.setPaymentDate(LocalDateTime.now());
                    log.info("💳 Pago con tarjeta completado - ID: {}", request.getTransactionId());
                } else {
                    payment.setStatus(PaymentStatus.FAILED);
                    log.warn("❌ Pago con tarjeta falló - Sin ID de transacción");
                }
            }
            case BANK_TRANSFER, MOBILE_PAYMENT -> {
                payment.setStatus(PaymentStatus.PENDING);
                log.info("⏳ Pago {} pendiente de confirmación", request.getPaymentMethod());
            }
            default -> {
                payment.setStatus(PaymentStatus.FAILED);
                log.error("❌ Método de pago no soportado: {}", request.getPaymentMethod());
            }
        }
    }

    private PaymentResponseDTO mapToPaymentResponseDTO(Payment payment) {
        return PaymentResponseDTO.builder()
                .id(payment.getId())
                .paymentCode(payment.getPaymentCode())
                .studentName(payment.getUser().getFirstName() + " " + payment.getUser().getLastName())
                .courseName(payment.getCourse().getTitle())
                .pricingType(payment.getPricingType())
                .studentCategory(payment.getStudentCategory())
                .quantityClasses(payment.getQuantityClasses())
                .personCount(payment.getPersonCount())
                .originalPrice(payment.getOriginalPrice())
                .discountAmount(payment.getDiscountAmount())
                .finalPrice(payment.getFinalPrice())
                .pricePerClass(payment.getPricePerClass())
                .paymentMethod(payment.getPaymentMethod())
                .status(payment.getStatus())
                .transactionId(payment.getTransactionId())
                .paymentDate(payment.getPaymentDate())
                .createdAt(payment.getCreatedAt())
                .notes(payment.getNotes())
                .build();
    }
}