package com.edidev.academyApp.security;

import com.edidev.academyApp.config.JwtProperties;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
@Slf4j
public class JwtUtils {

    @Autowired
    private JwtProperties jwtProperties;

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
