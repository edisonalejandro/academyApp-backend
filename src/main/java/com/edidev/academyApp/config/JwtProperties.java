package com.edidev.academyApp.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app.jwt")
@Data
public class JwtProperties {
    
    private String secret = "mySecretKeyForJWTTokenGenerationAndValidationThatIsSecureEnough256Bits";
    private long expirationMs = 86400000; // 24 horas en milisegundos
}
