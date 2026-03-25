package com.edidev.academyApp.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app.jwt")
@Data
public class JwtProperties {
    
    /**
     * JWT Secret - DEBE ser configurado vía variable de entorno JWT_SECRET
     * Usar al menos 256 bits (43+ caracteres base64)
     */
    private String secret;
    
    /**
     * Tiempo de expiración del token en milisegundos
     * Default: 24 horas (86400000 ms)
     */
    private long expirationMs = 86400000;
}
