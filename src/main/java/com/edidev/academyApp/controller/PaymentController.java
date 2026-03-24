package com.edidev.academyApp.controller;

import com.edidev.academyApp.dto.PaymentRequestDTO;
import com.edidev.academyApp.dto.PaymentResponseDTO;
import com.edidev.academyApp.dto.PricingCalculationDTO;
import com.edidev.academyApp.dto.PricingCalculationRequestDTO;
import com.edidev.academyApp.enums.PaymentStatus;
import com.edidev.academyApp.enums.StudentCategory;
import com.edidev.academyApp.service.PaymentService;
import com.edidev.academyApp.service.PricingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Slf4j
@Tag(name = "Payments", description = "💰 Gestión de pagos y precios de la academia")
public class PaymentController {

    private final PaymentService paymentService;
    private final PricingService pricingService;

    // ========== ENDPOINTS DE CÁLCULO DE PRECIOS ==========

    @GetMapping("/pricing/calculate")
    @Operation(summary = "💵 Calcular opciones de precios disponibles")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Opciones de precios calculadas exitosamente"),
        @ApiResponse(responseCode = "400", description = "Parámetros inválidos"),
        @ApiResponse(responseCode = "404", description = "Curso no encontrado")
    })
    public ResponseEntity<PricingCalculationDTO> calculatePricingOptions(
            @RequestParam Long courseId,
            @RequestParam StudentCategory studentCategory,
            @RequestParam(defaultValue = "1") Integer personCount) {
        
        log.info("🧮 Calculando precios para curso: {}, categoría: {}, personas: {}", 
                courseId, studentCategory, personCount);
        
        PricingCalculationDTO pricing = pricingService.calculatePricingOptions(
                courseId, studentCategory, personCount);
        
        return ResponseEntity.ok(pricing);
    }

    @PostMapping("/pricing/calculate")
    @Operation(summary = "🧮 Calcular precios con datos detallados")
    public ResponseEntity<PricingCalculationDTO> calculatePricingDetailed(
            @Valid @RequestBody PricingCalculationRequestDTO request) {
        
        log.info("🧮 Calculando precios detallados para: {}", request);
        
        PricingCalculationDTO pricing = pricingService.calculatePricingOptions(
                request.getCourseId(), 
                request.getStudentCategory(), 
                request.getPersonCount());
        
        return ResponseEntity.ok(pricing);
    }

    @GetMapping("/pricing/flexible")
    @Operation(summary = "⚡ Cálculo rápido de precio flexible")
    public ResponseEntity<Map<String, Object>> calculateFlexiblePricing(
            @RequestParam int numberOfClasses,
            @RequestParam StudentCategory studentCategory,
            @RequestParam(defaultValue = "false") boolean isCouple) {
        
        log.info("⚡ Cálculo flexible: {} clases, {}, pareja: {}", 
                numberOfClasses, studentCategory, isCouple);
        
        BigDecimal price = paymentService.calculatePaymentFlexible(
                numberOfClasses, studentCategory, isCouple);
        
        return ResponseEntity.ok(Map.of(
                "price", price,
                "currency", "CLP",
                "numberOfClasses", numberOfClasses,
                "studentCategory", studentCategory,
                "isCouple", isCouple,
                "pricePerClass", price.divide(BigDecimal.valueOf(numberOfClasses), 2, BigDecimal.ROUND_HALF_UP)
        ));
    }

    @GetMapping("/pricing/rule/{pricingRuleId}")
    @Operation(summary = "🎯 Calcular precio por regla específica")
    public ResponseEntity<Map<String, Object>> calculateByRule(@PathVariable Long pricingRuleId) {
        
        log.info("🎯 Calculando precio por regla: {}", pricingRuleId);
        
        BigDecimal price = paymentService.calculatePaymentByRule(pricingRuleId);
        
        return ResponseEntity.ok(Map.of(
                "price", price,
                "currency", "CLP",
                "pricingRuleId", pricingRuleId
        ));
    }

    // ========== ENDPOINTS DE PROCESAMIENTO DE PAGOS ==========

    @PostMapping("/process")
    @PreAuthorize("hasRole('STUDENT') or hasRole('ADMIN')")
    @Operation(summary = "💳 Procesar pago de clases")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Pago procesado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos de pago inválidos"),
        @ApiResponse(responseCode = "401", description = "Usuario no autenticado"),
        @ApiResponse(responseCode = "403", description = "Sin permisos para realizar pago"),
        @ApiResponse(responseCode = "404", description = "Curso o regla de precios no encontrada")
    })
    public ResponseEntity<PaymentResponseDTO> processPayment(
            @Valid @RequestBody PaymentRequestDTO paymentRequest,
            Authentication authentication) {
        
        String userEmail = authentication.getName();
        log.info("💳 Procesando pago para usuario: {}, curso: {}", userEmail, paymentRequest.getCourseId());
        
        PaymentResponseDTO response = paymentService.processPayment(paymentRequest, userEmail);
        
        log.info("✅ Pago procesado - Código: {}, Monto: ${}", 
                response.getPaymentCode(), response.getFinalPrice());
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/validate")
    @Operation(summary = "✔️ Validar datos de pago antes del procesamiento")
    public ResponseEntity<Map<String, Object>> validatePayment(
            @Valid @RequestBody PaymentRequestDTO paymentRequest,
            Authentication authentication) {
        
        String userEmail = authentication != null ? authentication.getName() : "anonymous";
        log.info("✔️ Validando pago para usuario: {}", userEmail);
        
        // Validar disponibilidad de regla de precios
        boolean isAvailable = paymentService.isPricingRuleAvailable(
                paymentRequest.getPricingRuleId(), 
                paymentRequest.getCourseId(), 
                paymentRequest.getPersonCount());
        
        if (!isAvailable) {
            return ResponseEntity.badRequest().body(Map.of(
                    "valid", false,
                    "message", "La regla de precios no está disponible"
            ));
        }
        
        // Calcular precio
        BigDecimal price = paymentService.calculatePaymentByRule(paymentRequest.getPricingRuleId());
        
        return ResponseEntity.ok(Map.of(
                "valid", true,
                "price", price,
                "currency", "CLP",
                "message", "Datos de pago válidos"
        ));
    }

    // ========== ENDPOINTS DE CONSULTA DE PAGOS ==========

    @GetMapping("/status/{paymentCode}")
    @Operation(summary = "📊 Consultar estado de pago por código")
    public ResponseEntity<PaymentResponseDTO> getPaymentStatus(@PathVariable String paymentCode) {
        
        log.info("📊 Consultando estado de pago: {}", paymentCode);
        
        PaymentResponseDTO payment = paymentService.getPaymentByCode(paymentCode);
        return ResponseEntity.ok(payment);
    }

    @GetMapping("/my-payments")
    @PreAuthorize("hasRole('STUDENT') or hasRole('TEACHER') or hasRole('ADMIN')")
    @Operation(summary = "📋 Obtener mis pagos")
    public ResponseEntity<List<PaymentResponseDTO>> getMyPayments(Authentication authentication) {
        
        String userEmail = authentication.getName();
        log.info("📋 Obteniendo pagos para usuario: {}", userEmail);
        
        List<PaymentResponseDTO> payments = paymentService.getPaymentsByUser(userEmail);
        
        log.info("📋 Encontrados {} pagos para usuario: {}", payments.size(), userEmail);
        return ResponseEntity.ok(payments);
    }

    @GetMapping("/course/{courseId}")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    @Operation(summary = "📚 Obtener pagos por curso")
    public ResponseEntity<List<PaymentResponseDTO>> getPaymentsByCourse(@PathVariable Long courseId) {
        
        log.info("📚 Obteniendo pagos para curso: {}", courseId);
        
        List<PaymentResponseDTO> payments = paymentService.getPaymentsByCourse(courseId);
        
        log.info("📚 Encontrados {} pagos para curso: {}", payments.size(), courseId);
        return ResponseEntity.ok(payments);
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "🔍 Buscar pagos con filtros")
    public ResponseEntity<List<PaymentResponseDTO>> searchPayments(
            @RequestParam(required = false) PaymentStatus status,
            @RequestParam(required = false) StudentCategory studentCategory,
            @RequestParam(required = false) String userEmail,
            @RequestParam(required = false) Long courseId) {
        
        log.info("🔍 Buscando pagos con filtros - Status: {}, Categoría: {}, Usuario: {}, Curso: {}", 
                status, studentCategory, userEmail, courseId);
        
        // Implementar lógica de búsqueda según filtros
        List<PaymentResponseDTO> payments;
        
        if (userEmail != null) {
            payments = paymentService.getPaymentsByUser(userEmail);
        } else if (courseId != null) {
            payments = paymentService.getPaymentsByCourse(courseId);
        } else {
            // Para búsquedas más complejas, necesitarías implementar métodos adicionales en el service
            payments = List.of(); // Placeholder
        }
        
        return ResponseEntity.ok(payments);
    }

    // ========== ENDPOINTS DE ADMINISTRACIÓN ==========

    @PatchMapping("/{paymentId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "✅ Actualizar estado de pago")
    public ResponseEntity<PaymentResponseDTO> updatePaymentStatus(
            @PathVariable Long paymentId,
            @RequestParam PaymentStatus status,
            @RequestParam(required = false) String notes,
            Authentication authentication) {
        
        String adminEmail = authentication.getName();
        log.info("✅ Admin {} actualizando pago {} a estado: {}", adminEmail, paymentId, status);
        
        PaymentResponseDTO payment = paymentService.updatePaymentStatus(paymentId, status, notes);
        
        log.info("✅ Pago {} actualizado exitosamente", payment.getPaymentCode());
        return ResponseEntity.ok(payment);
    }

    @PostMapping("/{paymentId}/refund")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "💸 Procesar reembolso de pago")
    public ResponseEntity<PaymentResponseDTO> refundPayment(
            @PathVariable Long paymentId,
            @RequestParam(required = false) String reason,
            Authentication authentication) {
        
        String adminEmail = authentication.getName();
        log.info("💸 Admin {} procesando reembolso para pago: {}", adminEmail, paymentId);
        
        String refundNotes = "Reembolso procesado por " + adminEmail + 
                           (reason != null ? ". Razón: " + reason : "");
        
        PaymentResponseDTO payment = paymentService.updatePaymentStatus(
                paymentId, PaymentStatus.REFUNDED, refundNotes);
        
        log.info("💸 Reembolso procesado para pago: {}", payment.getPaymentCode());
        return ResponseEntity.ok(payment);
    }

    // ========== ENDPOINTS DE REPORTES ==========

    @GetMapping("/reports/revenue")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "📈 Reporte de ingresos")
    public ResponseEntity<Map<String, Object>> getRevenueReport(
            @RequestParam String startDate,
            @RequestParam String endDate) {
        
        log.info("📈 Generando reporte de ingresos: {} - {}", startDate, endDate);
        
        Map<String, Object> report = paymentService.getRevenueReport(startDate, endDate);
        
        log.info("📈 Reporte generado - Total: ${}", report.get("totalRevenue"));
        return ResponseEntity.ok(report);
    }

    @GetMapping("/reports/revenue/breakdown")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "📊 Desglose detallado de ingresos")
    public ResponseEntity<Map<String, Object>> getRevenueBreakdown(
            @RequestParam String startDate,
            @RequestParam String endDate) {
        
        log.info("📊 Generando desglose de ingresos: {} - {}", startDate, endDate);
        
        java.time.LocalDateTime start = java.time.LocalDateTime.parse(startDate + "T00:00:00");
        java.time.LocalDateTime end = java.time.LocalDateTime.parse(endDate + "T23:59:59");
        
        Map<String, Object> breakdown = paymentService.getRevenueBreakdown(start, end);
        
        return ResponseEntity.ok(breakdown);
    }

    @GetMapping("/stats/summary")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "📋 Resumen estadístico de pagos")
    public ResponseEntity<Map<String, Object>> getPaymentsSummary() {
        
        log.info("📋 Generando resumen estadístico de pagos");
        
        // Implementar lógica de resumen estadístico
        Map<String, Object> summary = Map.of(
                "message", "Funcionalidad de resumen pendiente de implementar",
                "suggestion", "Agregar métodos estadísticos en PaymentService"
        );
        
        return ResponseEntity.ok(summary);
    }

    // ========== ENDPOINTS DE UTILIDAD ==========

    @GetMapping("/health")
    @Operation(summary = "🏥 Verificar salud del servicio de pagos")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        
        return ResponseEntity.ok(Map.of(
                "status", "healthy",
                "service", "PaymentController",
                "timestamp", java.time.LocalDateTime.now(),
                "version", "2.0"
        ));
    }

    @GetMapping("/pricing/rules/available")
    @Operation(summary = "📜 Obtener reglas de precios disponibles")
    public ResponseEntity<Map<String, Object>> getAvailablePricingRules(
            @RequestParam Long courseId,
            @RequestParam(required = false) StudentCategory studentCategory,
            @RequestParam(defaultValue = "1") Integer personCount) {
        
        log.info("📜 Obteniendo reglas disponibles para curso: {}", courseId);
        
        // Implementar lógica para obtener reglas disponibles
        Map<String, Object> rules = Map.of(
                "courseId", courseId,
                "message", "Funcionalidad pendiente de implementar",
                "suggestion", "Agregar método en PricingService para obtener reglas disponibles"
        );
        
        return ResponseEntity.ok(rules);
    }
}