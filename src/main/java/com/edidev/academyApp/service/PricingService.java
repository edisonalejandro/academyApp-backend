package com.edidev.academyApp.service;

import com.edidev.academyApp.dto.PricingCalculationDTO;
import com.edidev.academyApp.dto.PricingRuleDTO;
import com.edidev.academyApp.enums.PricingType;
import com.edidev.academyApp.enums.StudentCategory;
import com.edidev.academyApp.model.PricingRule;

import java.math.BigDecimal;
import java.util.List;

public interface PricingService {
    
    /**
     * Calcula todas las opciones de precios disponibles para un curso
     */
    PricingCalculationDTO calculatePricingOptions(Long courseId, StudentCategory studentCategory, Integer personCount);
    
    /**
     * Obtiene reglas de precios disponibles para un curso
     */
    List<PricingRuleDTO> getAvailablePricingRules(Long courseId, StudentCategory studentCategory, Integer personCount);
    
    /**
     * Obtiene regla de precios específica
     */
    PricingRuleDTO getPricingRule(Long pricingRuleId);
    
    /**
     * Crea nueva regla de precios
     */
    PricingRuleDTO createPricingRule(PricingRuleDTO pricingRuleDTO);
    
    /**
     * Actualiza regla de precios existente
     */
    PricingRuleDTO updatePricingRule(Long pricingRuleId, PricingRuleDTO pricingRuleDTO);
    
    /**
     * Desactiva regla de precios
     */
    void deactivatePricingRule(Long pricingRuleId);
    
    /**
     * Busca reglas de precios por criterios
     */
    List<PricingRuleDTO> searchPricingRules(PricingType pricingType, StudentCategory studentCategory, Boolean isActive);
    
    /**
     * Valida si una regla de precios está disponible
     */
    boolean isPricingRuleAvailable(Long pricingRuleId);
    
    /**
     * Calcula descuento aplicable
     */
    BigDecimal calculateDiscount(PricingRule pricingRule, Integer quantity);
}