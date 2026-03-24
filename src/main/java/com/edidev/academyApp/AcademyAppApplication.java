package com.edidev.academyApp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan("com.edidev.academyApp.model") // ✅ Especificar paquete de entidades
@EnableJpaRepositories("com.edidev.academyApp.repository") // ✅ Especificar paquete de repositorios
public class AcademyAppApplication {

    public static void main(String[] args) {
        SpringApplication.run(AcademyAppApplication.class, args);
    }
}
