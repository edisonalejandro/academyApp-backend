package com.edidev.academyApp.service.impl;

import com.edidev.academyApp.dto.PricingCalculationDTO;
import com.edidev.academyApp.dto.PricingRuleDTO;
import com.edidev.academyApp.enums.PricingType;
import com.edidev.academyApp.enums.StudentCategory;
import com.edidev.academyApp.exception.PricingRuleNotFoundException;
import com.edidev.academyApp.model.Course;
import com.edidev.academyApp.model.PricingRule;
import com.edidev.academyApp.repository.CourseRepository;
import com.edidev.academyApp.repository.PricingRuleRepository;
import com.edidev.academyApp.service.PricingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PricingServiceImpl implements PricingService {

    private final PricingRuleRepository pricingRuleRepository;
    private final CourseRepository courseRepository;

    /**
     * Calcula todas las opciones de precios disponibles para un curso
     */
    @Override
    public PricingCalculationDTO calculatePricingOptions(Long courseId, StudentCategory studentCategory, Integer personCount) {
        log.info("🧮 Calculando opciones de precios para curso: {}, categoría: {}, personas: {}", 
                courseId, studentCategory, personCount);

        // Validar que el curso existe
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Curso no encontrado: " + courseId));

        // Obtener todas las reglas de precios activas para la categoría y número de personas
        List<PricingRule> applicableRules = pricingRuleRepository
                .findByStudentCategoryAndPersonCountAndIsActiveTrueOrderByClassQuantityAsc(
                        studentCategory, personCount);

        if (applicableRules.isEmpty()) {
            log.warn("⚠️ No se encontraron reglas de precios para categoría: {} y personas: {}", 
                    studentCategory, personCount);
            
            // Crear opciones por defecto o lanzar excepción
            return createDefaultPricingOptions(course, studentCategory, personCount);
        }

        // Construir el DTO de respuesta
        List<PricingCalculationDTO.PricingOption> options = applicableRules.stream()
                .filter(this::isRuleCurrentlyValid)
                .map(this::mapToPricingOption)
                .collect(Collectors.toList());

        PricingCalculationDTO result = PricingCalculationDTO.builder()
                .courseId(courseId)
                .courseName(course.getTitle())
                .studentCategory(studentCategory)
                .personCount(personCount)
                .isCouple(personCount == 2)
                .options(options)
                .recommendedOptionId(findRecommendedOption(options))
                .calculatedAt(LocalDateTime.now())
                .build();

        log.info("✅ Calculadas {} opciones de precios para curso: {}", options.size(), course.getTitle());
        return result;
    }

    /**
     * Obtiene reglas de precios disponibles para un curso
     */
    @Override
    public List<PricingRuleDTO> getAvailablePricingRules(Long courseId, StudentCategory studentCategory, Integer personCount) {
        log.info("📜 Obteniendo reglas disponibles para curso: {}, categoría: {}, personas: {}", 
                courseId, studentCategory, personCount);

        // Validar curso
        courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Curso no encontrado: " + courseId));

        List<PricingRule> rules = pricingRuleRepository
                .findByStudentCategoryAndPersonCountAndIsActiveTrueOrderByClassQuantityAsc(
                        studentCategory, personCount);

        return rules.stream()
                .filter(this::isRuleCurrentlyValid)
                .map(this::mapToPricingRuleDTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene regla de precios específica
     */
    @Override
    public PricingRuleDTO getPricingRule(Long pricingRuleId) {
        log.info("🎯 Obteniendo regla de precios: {}", pricingRuleId);

        PricingRule rule = pricingRuleRepository.findById(pricingRuleId)
                .orElseThrow(() -> new PricingRuleNotFoundException("Regla de precios no encontrada: " + pricingRuleId));

        return mapToPricingRuleDTO(rule);
    }

    /**
     * Crea nueva regla de precios
     */
    @Override
    @Transactional
    public PricingRuleDTO createPricingRule(PricingRuleDTO pricingRuleDTO) {
        log.info("➕ Creando nueva regla de precios: {}", pricingRuleDTO.getName());

        // Validar que no existe una regla similar activa
        validateUniqueRule(pricingRuleDTO);

        // Calcular precio final
        BigDecimal finalPrice = calculateFinalPrice(
                pricingRuleDTO.getPrice(), 
                pricingRuleDTO.getDiscountPercentage()
        );

        PricingRule rule = PricingRule.builder()
                .name(pricingRuleDTO.getName())
                .description(pricingRuleDTO.getDescription())
                .pricingType(pricingRuleDTO.getPricingType())
                .studentCategory(pricingRuleDTO.getStudentCategory())
                .personCount(pricingRuleDTO.getPersonCount())
                .classQuantity(pricingRuleDTO.getClassQuantity())
                .price(pricingRuleDTO.getPrice())
                .discountPercentage(pricingRuleDTO.getDiscountPercentage())
                .finalPrice(finalPrice) // ✅ Ahora sí se establece correctamente
                .validFrom(pricingRuleDTO.getValidFrom())
                .validUntil(pricingRuleDTO.getValidUntil())
                .isActive(true)
                .build();

        PricingRule savedRule = pricingRuleRepository.save(rule);
        log.info("✅ Regla de precios creada: {} - ID: {}, Precio final: ${}", 
                savedRule.getName(), savedRule.getId(), savedRule.getFinalPrice());

        return mapToPricingRuleDTO(savedRule);
    }

    /**
     * Actualiza regla de precios existente
     */
    @Override
    @Transactional
    public PricingRuleDTO updatePricingRule(Long pricingRuleId, PricingRuleDTO pricingRuleDTO) {
        log.info("📝 Actualizando regla de precios: {}", pricingRuleId);

        PricingRule existingRule = pricingRuleRepository.findById(pricingRuleId)
                .orElseThrow(() -> new PricingRuleNotFoundException("Regla de precios no encontrada: " + pricingRuleId));

        // Usar el método helper para actualizar precio y recalcular
        existingRule.setPriceAndRecalculate(
                pricingRuleDTO.getPrice(), 
                pricingRuleDTO.getDiscountPercentage()
        );
        
        // Actualizar otros campos
        existingRule.setName(pricingRuleDTO.getName());
        existingRule.setDescription(pricingRuleDTO.getDescription());
        existingRule.setValidFrom(pricingRuleDTO.getValidFrom());
        existingRule.setValidUntil(pricingRuleDTO.getValidUntil());

        PricingRule savedRule = pricingRuleRepository.save(existingRule);
        log.info("✅ Regla de precios actualizada: {} - Precio final: ${}", 
                savedRule.getName(), savedRule.getFinalPrice());

        return mapToPricingRuleDTO(savedRule);
    }

    /**
     * Desactiva regla de precios
     */
    @Override
    @Transactional
    public void deactivatePricingRule(Long pricingRuleId) {
        log.info("🚫 Desactivando regla de precios: {}", pricingRuleId);

        PricingRule rule = pricingRuleRepository.findById(pricingRuleId)
                .orElseThrow(() -> new PricingRuleNotFoundException("Regla de precios no encontrada: " + pricingRuleId));

        rule.setIsActive(false);
        pricingRuleRepository.save(rule);

        log.info("✅ Regla de precios desactivada: {}", rule.getName());
    }

    /**
     * Busca reglas de precios por criterios
     */
    @Override
    public List<PricingRuleDTO> searchPricingRules(PricingType pricingType, StudentCategory studentCategory, Boolean isActive) {
        log.info("🔍 Buscando reglas - Tipo: {}, Categoría: {}, Activa: {}", 
                pricingType, studentCategory, isActive);

        List<PricingRule> rules = pricingRuleRepository.findByCriteria(pricingType, studentCategory, isActive);

        return rules.stream()
                .map(this::mapToPricingRuleDTO)
                .collect(Collectors.toList());
    }

    /**
     * Valida si una regla de precios está disponible
     */
    @Override
    public boolean isPricingRuleAvailable(Long pricingRuleId) {
        return pricingRuleRepository.findById(pricingRuleId)
                .map(this::isRuleCurrentlyValid)
                .orElse(false);
    }

    /**
     * Calcula descuento aplicable
     */
    @Override
    public BigDecimal calculateDiscount(PricingRule pricingRule, Integer quantity) {
        if (pricingRule.getDiscountPercentage() == null || pricingRule.getDiscountPercentage().compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal baseAmount = pricingRule.getPrice().multiply(BigDecimal.valueOf(quantity));
        return baseAmount.multiply(pricingRule.getDiscountPercentage())
                .divide(BigDecimal.valueOf(100), 2, BigDecimal.ROUND_HALF_UP);
    }

    // MÉTODOS PRIVADOS

    private boolean isRuleCurrentlyValid(PricingRule rule) {
        if (!rule.getIsActive()) {
            return false;
        }

        LocalDateTime now = LocalDateTime.now();
        
        if (rule.getValidFrom() != null && now.isBefore(rule.getValidFrom())) {
            return false;
        }

        if (rule.getValidUntil() != null && now.isAfter(rule.getValidUntil())) {
            return false;
        }

        return true;
    }

    private PricingCalculationDTO.PricingOption mapToPricingOption(PricingRule rule) {
        BigDecimal savings = rule.getPrice().subtract(rule.getFinalPrice());
        BigDecimal pricePerClass = rule.getFinalPrice().divide(
                BigDecimal.valueOf(rule.getClassQuantity()), 2, BigDecimal.ROUND_HALF_UP);

        return PricingCalculationDTO.PricingOption.builder()
                .pricingRuleId(rule.getId())
                .name(rule.getName())
                .description(rule.getDescription())
                .pricingType(rule.getPricingType())
                .classQuantity(rule.getClassQuantity())
                .originalPrice(rule.getPrice())
                .finalPrice(rule.getFinalPrice())
                .discountPercentage(rule.getDiscountPercentage())
                .savings(savings)
                .pricePerClass(pricePerClass)
                .isRecommended(false) // Se calculará después
                .validFrom(rule.getValidFrom())
                .validUntil(rule.getValidUntil())
                .build();
    }

    private Long findRecommendedOption(List<PricingCalculationDTO.PricingOption> options) {
        if (options.isEmpty()) {
            return null;
        }

        // Recomendar la opción con mejor precio por clase (más eficiente)
        return options.stream()
                .min((o1, o2) -> o1.getPricePerClass().compareTo(o2.getPricePerClass()))
                .map(PricingCalculationDTO.PricingOption::getPricingRuleId)
                .orElse(null);
    }

    private PricingCalculationDTO createDefaultPricingOptions(Course course, StudentCategory studentCategory, Integer personCount) {
        // Crear opciones por defecto basadas en precios fijos
        List<PricingCalculationDTO.PricingOption> defaultOptions = new ArrayList<>();
        
        // Agregar opción de clase individual
        defaultOptions.add(createDefaultOption("Clase Individual", PricingType.SINGLE_CLASS, 1, 
                getDefaultPrice(studentCategory, 1, personCount)));

        return PricingCalculationDTO.builder()
                .courseId(course.getId())
                .courseName(course.getTitle())
                .studentCategory(studentCategory)
                .personCount(personCount)
                .isCouple(personCount == 2)
                .options(defaultOptions)
                .recommendedOptionId(defaultOptions.get(0).getPricingRuleId())
                .calculatedAt(LocalDateTime.now())
                .hasDefaultPricing(true)
                .build();
    }

    private PricingCalculationDTO.PricingOption createDefaultOption(String name, PricingType type, 
                                                                   Integer quantity, BigDecimal price) {
        return PricingCalculationDTO.PricingOption.builder()
                .pricingRuleId(-1L) // ID temporal para opciones por defecto
                .name(name)
                .description("Precio estándar")
                .pricingType(type)
                .classQuantity(quantity)
                .originalPrice(price)
                .finalPrice(price)
                .discountPercentage(BigDecimal.ZERO)
                .savings(BigDecimal.ZERO)
                .pricePerClass(price.divide(BigDecimal.valueOf(quantity), 2, BigDecimal.ROUND_HALF_UP))
                .isRecommended(true)
                .build();
    }

    private BigDecimal getDefaultPrice(StudentCategory category, Integer quantity, Integer personCount) {
        // Precios por defecto basados en la lógica legacy
        int basePrice = switch (category) {
            case UNIVERSITY -> 2000;
            case REGULAR -> 4000;
            default -> 4000;
        };

        int totalPrice = basePrice * quantity;
        
        // Ajuste para parejas
        if (personCount == 2 && quantity == 8) {
            totalPrice = 40000; // Precio especial para parejas
        }

        return BigDecimal.valueOf(totalPrice);
    }

    private void validateUniqueRule(PricingRuleDTO pricingRuleDTO) {
        boolean exists = pricingRuleRepository.existsByPricingTypeAndStudentCategoryAndPersonCountAndIsActiveTrue(
                pricingRuleDTO.getPricingType(),
                pricingRuleDTO.getStudentCategory(),
                pricingRuleDTO.getPersonCount()
        );

        if (exists) {
            throw new RuntimeException("Ya existe una regla activa para estos criterios");
        }
    }

    private BigDecimal calculateFinalPrice(BigDecimal price, BigDecimal discountPercentage) {
        if (price == null) {
            return BigDecimal.ZERO;
        }
        
        if (discountPercentage == null || discountPercentage.compareTo(BigDecimal.ZERO) <= 0) {
            return price;
        }

        BigDecimal discount = price.multiply(discountPercentage)
                .divide(BigDecimal.valueOf(100), 2, BigDecimal.ROUND_HALF_UP);
        
        return price.subtract(discount);
    }

    private PricingRuleDTO mapToPricingRuleDTO(PricingRule rule) {
        return PricingRuleDTO.builder()
                .id(rule.getId())
                .name(rule.getName())
                .description(rule.getDescription())
                .pricingType(rule.getPricingType())
                .studentCategory(rule.getStudentCategory())
                .personCount(rule.getPersonCount())
                .classQuantity(rule.getClassQuantity())
                .price(rule.getPrice())
                .discountPercentage(rule.getDiscountPercentage())
                .finalPrice(rule.getFinalPrice())
                .validFrom(rule.getValidFrom())
                .validUntil(rule.getValidUntil())
                .isActive(rule.getIsActive())
                .createdAt(rule.getCreatedAt())
                .updatedAt(rule.getUpdatedAt())
                .build();
    }
}