package com.edidev.academyApp.config;

import com.edidev.academyApp.model.Role;
import com.edidev.academyApp.model.RoleName;
import com.edidev.academyApp.model.User;
import com.edidev.academyApp.repository.UserRepository;
import com.edidev.academyApp.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleService roleService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Inicializar roles por defecto
        roleService.initializeDefaultRoles();
        System.out.println("Roles por defecto inicializados correctamente");

        // Crear usuario administrador por defecto si no existe
        if (!userRepository.existsByEmail("admin@academy.com")) {
            User admin = new User(
                "Administrador",
                "Sistema",
                "admin@academy.com",
                passwordEncoder.encode("admin123")
            );
            
            Role adminRole = roleService.findByName(RoleName.ADMIN)
                    .orElseThrow(() -> new RuntimeException("Rol ADMIN no encontrado"));
            admin.getRoles().add(adminRole);
            
            userRepository.save(admin);
            System.out.println("Usuario administrador creado: admin@academy.com / admin123");
        }
    }
}
