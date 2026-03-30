package com.edidev.academyApp.controller;

import com.edidev.academyApp.dto.PricingCalculationDTO;
import com.edidev.academyApp.dto.PricingRuleDTO;
import com.edidev.academyApp.dto.PricingCalculationRequestDTO;
import com.edidev.academyApp.enums.PricingType;
import com.edidev.academyApp.enums.StudentCategory;
import com.edidev.academyApp.service.PricingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/pricing")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Pricing", description = "💵 Gestión de precios y tarifas")
public class PricingController {

    private final PricingService pricingService;

    // ========== ENDPOINTS DE CÁLCULO DE PRECIOS ==========

    @GetMapping("/calculate")
    @Operation(summary = "🧮 Calcular opciones de precios para un curso")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Opciones de precios calculadas exitosamente"),
        @ApiResponse(responseCode = "400", description = "Parámetros inválidos"),
        @ApiResponse(responseCode = "404", description = "Curso no encontrado")
    })
    public ResponseEntity<PricingCalculationDTO> calculatePricing(
            @RequestParam Long courseId,
            @RequestParam StudentCategory studentCategory,
            @RequestParam(defaultValue = "1") Integer personCount) {
        
        log.info("🧮 Calculando precios - Curso: {}, Categoría: {}, Personas: {}", 
                courseId, studentCategory, personCount);
        
        PricingCalculationDTO calculation = pricingService.calculatePricingOptions(
                courseId, studentCategory, personCount);
        
        log.info("✅ Calculadas {} opciones de precios", calculation.getOptions().size());
        return ResponseEntity.ok(calculation);
    }

    @PostMapping("/calculate")
    @Operation(summary = "🧮 Calcular precios con datos detallados")
    public ResponseEntity<PricingCalculationDTO> calculatePricingDetailed(
            @Valid @RequestBody PricingCalculationRequestDTO request) {
        
        log.info("🧮 Calculando precios detallados: {}", request);
        
        PricingCalculationDTO calculation = pricingService.calculatePricingOptions(
                request.getCourseId(), 
                request.getStudentCategory(), 
                request.getPersonCount());
        
        return ResponseEntity.ok(calculation);
    }

    @GetMapping("/quick-quote")
    @Operation(summary = "⚡ Cotización rápida sin curso específico")
    public ResponseEntity<Map<String, Object>> getQuickQuote(
            @RequestParam StudentCategory studentCategory,
            @RequestParam(defaultValue = "1") Integer personCount) {
        
        log.info("⚡ Cotización rápida - Categoría: {}, Personas: {}", studentCategory, personCount);
        
        // Obtener reglas disponibles para esta categoría
        List<PricingRuleDTO> availableRules = pricingService.getAvailablePricingRules(
                null, studentCategory, personCount);
        
        return ResponseEntity.ok(Map.of(
                "studentCategory", studentCategory,
                "personCount", personCount,
                "availableOptions", availableRules,
                "totalOptions", availableRules.size(),
                "message", availableRules.isEmpty() ? 
                    "No hay opciones disponibles para esta configuración" :
                    "Opciones disponibles encontradas"
        ));
    }

    // ========== ENDPOINTS DE GESTIÓN DE REGLAS ==========

    @GetMapping("/rules")
    @Operation(summary = "📋 Obtener todas las reglas de precios activas")
    public ResponseEntity<List<PricingRuleDTO>> getAllPricingRules(
            @RequestParam(required = false) StudentCategory studentCategory,
            @RequestParam(required = false) PricingType pricingType,
            @RequestParam(defaultValue = "true") Boolean activeOnly) {
        
        log.info("📋 Obteniendo reglas - Categoría: {}, Tipo: {}, Solo activas: {}", 
                studentCategory, pricingType, activeOnly);
        
        List<PricingRuleDTO> rules = pricingService.searchPricingRules(
                pricingType, studentCategory, activeOnly);
        
        log.info("📋 Encontradas {} reglas de precios", rules.size());
        return ResponseEntity.ok(rules);
    }

    @GetMapping("/rules/{ruleId}")
    @Operation(summary = "🎯 Obtener regla de precios específica")
    public ResponseEntity<PricingRuleDTO> getPricingRule(@PathVariable Long ruleId) {
        
        log.info("🎯 Obteniendo regla de precios: {}", ruleId);
        
        PricingRuleDTO rule = pricingService.getPricingRule(ruleId);
        return ResponseEntity.ok(rule);
    }

    @GetMapping("/rules/available")
    @Operation(summary = "📜 Obtener reglas disponibles para curso específico")
    public ResponseEntity<List<PricingRuleDTO>> getAvailablePricingRules(
            @RequestParam Long courseId,
            @RequestParam StudentCategory studentCategory,
            @RequestParam(defaultValue = "1") Integer personCount) {
        
        log.info("📜 Obteniendo reglas disponibles - Curso: {}, Categoría: {}, Personas: {}", 
                courseId, studentCategory, personCount);
        
        List<PricingRuleDTO> rules = pricingService.getAvailablePricingRules(
                courseId, studentCategory, personCount);
        
        log.info("📜 Encontradas {} reglas disponibles", rules.size());
        return ResponseEntity.ok(rules);
    }

    @GetMapping("/rules/category/{category}")
    @Operation(summary = "🏷️ Obtener reglas por categoría de estudiante")
    public ResponseEntity<List<PricingRuleDTO>> getPricingRulesByCategory(
            @PathVariable StudentCategory category,
            @RequestParam(defaultValue = "1") Integer personCount) {
        
        log.info("🏷️ Obteniendo reglas por categoría: {}, personas: {}", category, personCount);
        
        List<PricingRuleDTO> rules = pricingService.getAvailablePricingRules(
                null, category, personCount);
        
        return ResponseEntity.ok(rules);
    }

    // ========== ENDPOINTS DE ADMINISTRACIÓN ==========

    @PostMapping("/rules")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "➕ Crear nueva regla de precios")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Regla creada exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos"),
        @ApiResponse(responseCode = "403", description = "Sin permisos de administrador"),
        @ApiResponse(responseCode = "409", description = "Ya existe una regla similar")
    })
    public ResponseEntity<PricingRuleDTO> createPricingRule(
            @Valid @RequestBody PricingRuleDTO pricingRuleDTO,
            Authentication authentication) {
        
        String adminEmail = authentication.getName();
        log.info("➕ Admin {} creando regla de precios: {}", adminEmail, pricingRuleDTO.getName());
        
        PricingRuleDTO savedRule = pricingService.createPricingRule(pricingRuleDTO);
        
        log.info("✅ Regla creada exitosamente - ID: {}, Nombre: {}", 
                savedRule.getId(), savedRule.getName());
        
        return ResponseEntity.status(HttpStatus.CREATED).body(savedRule);
    }

    @PutMapping("/rules/{ruleId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "✏️ Actualizar regla de precios existente")
    public ResponseEntity<PricingRuleDTO> updatePricingRule(
            @PathVariable Long ruleId,
            @Valid @RequestBody PricingRuleDTO pricingRuleDTO,
            Authentication authentication) {
        
        String adminEmail = authentication.getName();
        log.info("✏️ Admin {} actualizando regla: {}", adminEmail, ruleId);
        
        PricingRuleDTO updatedRule = pricingService.updatePricingRule(ruleId, pricingRuleDTO);
        
        log.info("✅ Regla actualizada - ID: {}, Precio final: ${}", 
                updatedRule.getId(), updatedRule.getFinalPrice());
        
        return ResponseEntity.ok(updatedRule);
    }

    @PatchMapping("/rules/{ruleId}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "✅ Activar regla de precios")
    public ResponseEntity<PricingRuleDTO> activatePricingRule(
            @PathVariable Long ruleId,
            Authentication authentication) {
        
        String adminEmail = authentication.getName();
        log.info("✅ Admin {} activando regla: {}", adminEmail, ruleId);
        
        PricingRuleDTO rule = pricingService.getPricingRule(ruleId);
        rule.setIsActive(true);
        PricingRuleDTO updatedRule = pricingService.updatePricingRule(ruleId, rule);
        
        log.info("✅ Regla activada: {}", updatedRule.getName());
        return ResponseEntity.ok(updatedRule);
    }

    @PatchMapping("/rules/{ruleId}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "🚫 Desactivar regla de precios")
    public ResponseEntity<Map<String, Object>> deactivatePricingRule(
            @PathVariable Long ruleId,
            Authentication authentication) {
        
        String adminEmail = authentication.getName();
        log.info("🚫 Admin {} desactivando regla: {}", adminEmail, ruleId);
        
        pricingService.deactivatePricingRule(ruleId);
        
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Regla de precios desactivada exitosamente",
                "ruleId", ruleId,
                "deactivatedBy", adminEmail
        ));
    }

    @DeleteMapping("/rules/{ruleId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "🗑️ Eliminar regla de precios (desactivar)")
    public ResponseEntity<Map<String, Object>> deletePricingRule(
            @PathVariable Long ruleId,
            Authentication authentication) {
        
        // En lugar de eliminar físicamente, desactivamos la regla
        return deactivatePricingRule(ruleId, authentication);
    }

    // ========== ENDPOINTS DE VALIDACIÓN ==========

    @GetMapping("/rules/{ruleId}/validate")
    @Operation(summary = "✔️ Validar disponibilidad de regla de precios")
    public ResponseEntity<Map<String, Object>> validatePricingRule(@PathVariable Long ruleId) {
        
        log.info("✔️ Validando disponibilidad de regla: {}", ruleId);
        
        boolean isAvailable = pricingService.isPricingRuleAvailable(ruleId);
        PricingRuleDTO rule = null;
        
        if (isAvailable) {
            rule = pricingService.getPricingRule(ruleId);
        }
        
        return ResponseEntity.ok(Map.of(
                "ruleId", ruleId,
                "isAvailable", isAvailable,
                "isValid", isAvailable && (rule != null && rule.isCurrentlyValid()),
                "rule", rule != null ? rule : "Regla no disponible",
                "message", isAvailable ? "Regla disponible" : "Regla no disponible o expirada"
        ));
    }

    @PostMapping("/rules/bulk-validate")
    @Operation(summary = "✅ Validar múltiples reglas de precios")
    public ResponseEntity<Map<String, Object>> validateMultiplePricingRules(
            @RequestBody List<Long> ruleIds) {
        
        log.info("✅ Validando {} reglas de precios", ruleIds.size());
        
        Map<Long, Boolean> validationResults = ruleIds.stream()
                .collect(java.util.stream.Collectors.toMap(
                        id -> id,
                        pricingService::isPricingRuleAvailable
                ));
        
        long validRules = validationResults.values().stream()
                .mapToLong(valid -> valid ? 1 : 0)
                .sum();
        
        return ResponseEntity.ok(Map.of(
                "totalRules", ruleIds.size(),
                "validRules", validRules,
                "invalidRules", ruleIds.size() - validRules,
                "validationResults", validationResults
        ));
    }

    // ========== ENDPOINTS DE REPORTES ==========

    @GetMapping("/reports/summary")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "📊 Resumen de reglas de precios")
    public ResponseEntity<Map<String, Object>> getPricingSummary() {
        
        log.info("📊 Generando resumen de reglas de precios");
        
        List<PricingRuleDTO> allRules = pricingService.searchPricingRules(null, null, null);
        
        long activeRules = allRules.stream().filter(PricingRuleDTO::getIsActive).count();
        long inactiveRules = allRules.size() - activeRules;
        
        Map<StudentCategory, Long> rulesByCategory = allRules.stream()
                .filter(PricingRuleDTO::getIsActive)
                .collect(java.util.stream.Collectors.groupingBy(
                        PricingRuleDTO::getStudentCategory,
                        java.util.stream.Collectors.counting()
                ));
        
        Map<PricingType, Long> rulesByType = allRules.stream()
                .filter(PricingRuleDTO::getIsActive)
                .collect(java.util.stream.Collectors.groupingBy(
                        PricingRuleDTO::getPricingType,
                        java.util.stream.Collectors.counting()
                ));
        
        return ResponseEntity.ok(Map.of(
                "totalRules", allRules.size(),
                "activeRules", activeRules,
                "inactiveRules", inactiveRules,
                "rulesByCategory", rulesByCategory,
                "rulesByType", rulesByType,
                "generatedAt", java.time.LocalDateTime.now()
        ));
    }

    // ========== ENDPOINTS DE UTILIDAD ==========

    @GetMapping("/health")
    @Operation(summary = "🏥 Verificar salud del servicio de precios")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        
        try {
            // Verificar que el servicio responde
            List<PricingRuleDTO> testRules = pricingService.searchPricingRules(null, null, true);
            
            return ResponseEntity.ok(Map.of(
                    "status", "healthy",
                    "service", "PricingController",
                    "activeRulesCount", testRules.size(),
                    "timestamp", java.time.LocalDateTime.now(),
                    "version", "2.0"
            ));
        } catch (Exception e) {
            log.error("❌ Error en health check: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of(
                            "status", "unhealthy",
                            "error", e.getMessage(),
                            "timestamp", java.time.LocalDateTime.now()
                    ));
        }
    }

    @GetMapping("/types")
    @Operation(summary = "📝 Obtener tipos de precios disponibles")
    public ResponseEntity<Map<String, Object>> getPricingTypes() {
        
        return ResponseEntity.ok(Map.of(
                "pricingTypes", PricingType.values(),
                "studentCategories", StudentCategory.values(),
                "personCountOptions", List.of(1, 2),
                "description", Map.of(
                        "pricingTypes", "Tipos de paquetes disponibles",
                        "studentCategories", "Categorías de estudiantes",
                        "personCountOptions", "Opciones de número de personas"
                )
        ));
    }
}