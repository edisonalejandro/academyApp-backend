package com.edidev.academyApp.config;

import com.edidev.academyApp.enums.*;
import com.edidev.academyApp.model.*;
import com.edidev.academyApp.repository.*;
import com.edidev.academyApp.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleService roleService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CourseRepository courseRepository;
    private final PricingRuleRepository pricingRuleRepository;

    @Override
    public void run(String... args) throws Exception {
        // Inicializar roles por defecto
        roleService.initializeDefaultRoles();
        System.out.println("Roles por defecto inicializados correctamente");

        // Crear usuario administrador por defecto si no existe
        User admin = null;
        if (!userRepository.existsByEmail("admin@academy.com")) {
            admin = new User(
                "Administrador",
                "Sistema",
                "admin@academy.com",
                passwordEncoder.encode("admin123")
            );
            
            Role adminRole = roleService.findByName(RoleName.ADMIN)
                    .orElseThrow(() -> new RuntimeException("Rol ADMIN no encontrado"));
            admin.getRoles().add(adminRole);
            
            admin = userRepository.save(admin);
            System.out.println("Usuario administrador creado: admin@academy.com / admin123");
        } else {
            admin = userRepository.findByEmail("admin@academy.com")
                    .orElseThrow(() -> new RuntimeException("Admin no encontrado"));
        }

        // Crear curso de ejemplo si no existe
        if (courseRepository.count() == 0) {
            Course salsaCourse = Course.builder()
                    .title("Salsa Intermedia")
                    .code("SALSA-INT-001")
                    .description("Curso de salsa nivel intermedio para estudiantes con conocimientos básicos")
                    .danceType(DanceType.SALSA)
                    .level(DanceLevel.INTERMEDIATE)
                    .pricePerHour(BigDecimal.valueOf(50))
                    .durationHours(BigDecimal.valueOf(1.5))
                    .maxCapacity(20)
                    .teacher(admin)
                    .isActive(true)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            
            courseRepository.save(salsaCourse);
            System.out.println("Curso de ejemplo creado: " + salsaCourse.getTitle());
        }

        // Crear reglas de precios por defecto si no existen
        if (pricingRuleRepository.count() == 0) {
            createDefaultPricingRules();
            System.out.println("Reglas de precios por defecto creadas correctamente");
        }
    }

    private void createDefaultPricingRules() {
        LocalDateTime now = LocalDateTime.now();

        // Reglas para REGULAR - Individual
        pricingRuleRepository.save(PricingRule.builder()
                .name("Regular - 1 Clase Individual")
                .pricingType(PricingType.SINGLE_CLASS)
                .classQuantity(1)
                .studentCategory(StudentCategory.REGULAR)
                .personCount(1)
                .price(BigDecimal.valueOf(4000))
                .finalPrice(BigDecimal.valueOf(4000))
                .discountPercentage(BigDecimal.ZERO)
                .isActive(true)
                .createdAt(now)
                .updatedAt(now)
                .build());

        pricingRuleRepository.save(PricingRule.builder()
                .name("Regular - 4 Clases Individual")
                .pricingType(PricingType.PACKAGE_4)
                .classQuantity(4)
                .studentCategory(StudentCategory.REGULAR)
                .personCount(1)
                .price(BigDecimal.valueOf(16000))
                .finalPrice(BigDecimal.valueOf(15000))
                .discountPercentage(BigDecimal.valueOf(6.25))
                .isActive(true)
                .createdAt(now)
                .updatedAt(now)
                .build());

        pricingRuleRepository.save(PricingRule.builder()
                .name("Regular - 8 Clases Individual")
                .pricingType(PricingType.PACKAGE_8)
                .classQuantity(8)
                .studentCategory(StudentCategory.REGULAR)
                .personCount(1)
                .price(BigDecimal.valueOf(32000))
                .finalPrice(BigDecimal.valueOf(28000))
                .discountPercentage(BigDecimal.valueOf(12.5))
                .isActive(true)
                .createdAt(now)
                .updatedAt(now)
                .build());

        // Reglas para UNIVERSITY - Individual
        pricingRuleRepository.save(PricingRule.builder()
                .name("Universitario - 1 Clase Individual")
                .pricingType(PricingType.SINGLE_CLASS)
                .classQuantity(1)
                .studentCategory(StudentCategory.UNIVERSITY)
                .personCount(1)
                .price(BigDecimal.valueOf(2000))
                .finalPrice(BigDecimal.valueOf(2000))
                .discountPercentage(BigDecimal.ZERO)
                .isActive(true)
                .createdAt(now)
                .updatedAt(now)
                .build());

        pricingRuleRepository.save(PricingRule.builder()
                .name("Universitario - 4 Clases Individual")
                .pricingType(PricingType.PACKAGE_4)
                .classQuantity(4)
                .studentCategory(StudentCategory.UNIVERSITY)
                .personCount(1)
                .price(BigDecimal.valueOf(8000))
                .finalPrice(BigDecimal.valueOf(7500))
                .discountPercentage(BigDecimal.valueOf(6.25))
                .isActive(true)
                .createdAt(now)
                .updatedAt(now)
                .build());

        pricingRuleRepository.save(PricingRule.builder()
                .name("Universitario - 8 Clases Individual")
                .pricingType(PricingType.PACKAGE_8)
                .classQuantity(8)
                .studentCategory(StudentCategory.UNIVERSITY)
                .personCount(1)
                .price(BigDecimal.valueOf(16000))
                .finalPrice(BigDecimal.valueOf(14000))
                .discountPercentage(BigDecimal.valueOf(12.5))
                .isActive(true)
                .createdAt(now)
                .updatedAt(now)
                .build());

        // Reglas para REGULAR - Pareja
        pricingRuleRepository.save(PricingRule.builder()
                .name("Regular - 1 Clase Pareja")
                .pricingType(PricingType.SINGLE_CLASS)
                .classQuantity(1)
                .studentCategory(StudentCategory.REGULAR)
                .personCount(2)
                .price(BigDecimal.valueOf(6000))
                .finalPrice(BigDecimal.valueOf(6000))
                .discountPercentage(BigDecimal.ZERO)
                .isActive(true)
                .createdAt(now)
                .updatedAt(now)
                .build());

        pricingRuleRepository.save(PricingRule.builder()
                .name("Regular - 4 Clases Pareja")
                .pricingType(PricingType.PACKAGE_4)
                .classQuantity(4)
                .studentCategory(StudentCategory.REGULAR)
                .personCount(2)
                .price(BigDecimal.valueOf(24000))
                .finalPrice(BigDecimal.valueOf(22000))
                .discountPercentage(BigDecimal.valueOf(8.33))
                .isActive(true)
                .createdAt(now)
                .updatedAt(now)
                .build());

        pricingRuleRepository.save(PricingRule.builder()
                .name("Regular - 8 Clases Pareja")
                .pricingType(PricingType.PACKAGE_8)
                .classQuantity(8)
                .studentCategory(StudentCategory.REGULAR)
                .personCount(2)
                .price(BigDecimal.valueOf(48000))
                .finalPrice(BigDecimal.valueOf(40000))
                .discountPercentage(BigDecimal.valueOf(16.67))
                .isActive(true)
                .createdAt(now)
                .updatedAt(now)
                .build());

        // Reglas para UNIVERSITY - Pareja
        pricingRuleRepository.save(PricingRule.builder()
                .name("Universitario - 1 Clase Pareja")
                .pricingType(PricingType.SINGLE_CLASS)
                .classQuantity(1)
                .studentCategory(StudentCategory.UNIVERSITY)
                .personCount(2)
                .price(BigDecimal.valueOf(3000))
                .finalPrice(BigDecimal.valueOf(3000))
                .discountPercentage(BigDecimal.ZERO)
                .isActive(true)
                .createdAt(now)
                .updatedAt(now)
                .build());

        pricingRuleRepository.save(PricingRule.builder()
                .name("Universitario - 4 Clases Pareja")
                .pricingType(PricingType.PACKAGE_4)
                .classQuantity(4)
                .studentCategory(StudentCategory.UNIVERSITY)
                .personCount(2)
                .price(BigDecimal.valueOf(12000))
                .finalPrice(BigDecimal.valueOf(11000))
                .discountPercentage(BigDecimal.valueOf(8.33))
                .isActive(true)
                .createdAt(now)
                .updatedAt(now)
                .build());

        pricingRuleRepository.save(PricingRule.builder()
                .name("Universitario - 8 Clases Pareja")
                .pricingType(PricingType.PACKAGE_8)
                .classQuantity(8)
                .studentCategory(StudentCategory.UNIVERSITY)
                .personCount(2)
                .price(BigDecimal.valueOf(24000))
                .finalPrice(BigDecimal.valueOf(20000))
                .discountPercentage(BigDecimal.valueOf(16.67))
                .isActive(true)
                .createdAt(now)
                .updatedAt(now)
                .build());
    }
}
