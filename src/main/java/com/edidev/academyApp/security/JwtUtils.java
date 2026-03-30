package com.edidev.academyApp.security;

import com.edidev.academyApp.config.JwtProperties;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Arrays;
import java.util.Date;

@Component
@Slf4j
public class JwtUtils {

    @Autowired
    private JwtProperties jwtProperties;

    @Autowired
    private Environment environment;

    /**
     * Valida al arranque que el JWT secret no sea el valor por defecto en producción.
     * Si se detecta el valor "changeme" con perfil "prod", el arranque falla de inmediato
     * evitando un despliegue con una clave de firma predecible.
     */
    @PostConstruct
    public void validateJwtSecret() {
        String secret = jwtProperties.getSecret();
        boolean isProd = Arrays.asList(environment.getActiveProfiles()).contains("prod");
        if (isProd && (secret == null || secret.toLowerCase().contains("changeme"))) {
            throw new IllegalStateException(
                "[SEGURIDAD] JWT_SECRET debe configurarse como variable de entorno en producción. "
                + "No se permite usar el valor por defecto 'changeme' en el perfil 'prod'."
            );
        }
    }

    private SecretKey getSigningKey() {
        // Asegurar que la clave tenga al menos 256 bits (32 bytes)
        String secret = jwtProperties.getSecret();
        if (secret.length() < 32) {
            // Expandir la clave si es muy corta
            secret = secret + "0123456789ABCDEF".repeat((32 - secret.length()) / 16 + 1);
            secret = secret.substring(0, 32);
        }
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String generateJwtToken(Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        return generateTokenFromUsername(userPrincipal.getUsername());
    }

    public String generateTokenFromUsername(String username) {
        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date((new Date()).getTime() + jwtProperties.getExpirationMs()))
                .signWith(getSigningKey())
                .compact();
    }

    public String getUserNameFromJwtToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(authToken);
            return true;
        } catch (MalformedJwtException e) {
            log.error("JWT token inválido: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.error("JWT token expirado: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("JWT token no soportado: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string está vacío: {}", e.getMessage());
        }
        return false;
    }
}
