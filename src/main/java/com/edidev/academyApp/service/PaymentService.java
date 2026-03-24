package com.edidev.academyApp.service;

import com.edidev.academyApp.dto.PaymentRequestDTO;
import com.edidev.academyApp.dto.PaymentResponseDTO;
import com.edidev.academyApp.enums.PaymentStatus;
import com.edidev.academyApp.enums.StudentCategory;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface PaymentService {
    
    /**
     * Calcula pago usando el sistema flexible de reglas de precios
     */
    BigDecimal calculatePaymentFlexible(int numberOfClasses, StudentCategory studentCategory, boolean isCouple);
    
    /**
     * Calcula pago usando regla de precios específica
     */
    BigDecimal calculatePaymentByRule(Long pricingRuleId);
    
    /**
     * Procesa un nuevo pago completo
     */
    PaymentResponseDTO processPayment(PaymentRequestDTO request, String userEmail);
    
    /**
     * Obtiene un pago por código
     */
    PaymentResponseDTO getPaymentByCode(String paymentCode);
    
    /**
     * Obtiene los pagos de un usuario
     */
    List<PaymentResponseDTO> getPaymentsByUser(String userEmail);
    
    /**
     * Obtiene los pagos de un curso
     */
    List<PaymentResponseDTO> getPaymentsByCourse(Long courseId);
    
    /**
     * Actualiza el estado de un pago
     */
    PaymentResponseDTO updatePaymentStatus(Long paymentId, PaymentStatus status, String notes);
    
    /**
     * Genera reporte de ingresos
     */
    Map<String, Object> getRevenueReport(String startDate, String endDate);
    
    /**
     * Obtiene desglose detallado de ingresos
     */
    Map<String, Object> getRevenueBreakdown(LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Valida disponibilidad de una regla de precios
     */
    boolean isPricingRuleAvailable(Long pricingRuleId, Long courseId, Integer personCount);
}