package com.edidev.academyApp.security;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Filtro de rate limiting para los endpoints de autenticación.
 *
 * Límites aplicados por IP:
 *  - /api/auth/login    → 10 intentos por minuto
 *  - /api/auth/register → 5 intentos por minuto
 *
 * Los buckets se almacenan en memoria. Para entornos con múltiples instancias,
 * reemplazar el ConcurrentHashMap por un backend distribuido (Redis + bucket4j-redis).
 */
@Component
@Slf4j
public class RateLimitingFilter extends OncePerRequestFilter {

    private static final String LOGIN_PATH    = "/api/auth/login";
    private static final String REGISTER_PATH = "/api/auth/register";

    // Un bucket por IP para login y otro para register
    private final Map<String, Bucket> loginBuckets    = new ConcurrentHashMap<>();
    private final Map<String, Bucket> registerBuckets = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();

        if (path.equals(LOGIN_PATH) && "POST".equalsIgnoreCase(request.getMethod())) {
            String ip = resolveClientIp(request);
            Bucket bucket = loginBuckets.computeIfAbsent(ip, k -> buildLoginBucket());
            if (!bucket.tryConsume(1)) {
                log.warn("Rate limit excedido en /login para IP: {}", ip);
                sendTooManyRequests(response);
                return;
            }
        } else if (path.equals(REGISTER_PATH) && "POST".equalsIgnoreCase(request.getMethod())) {
            String ip = resolveClientIp(request);
            Bucket bucket = registerBuckets.computeIfAbsent(ip, k -> buildRegisterBucket());
            if (!bucket.tryConsume(1)) {
                log.warn("Rate limit excedido en /register para IP: {}", ip);
                sendTooManyRequests(response);
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    /** 10 intentos por minuto con recarga gradual */
    private Bucket buildLoginBucket() {
        Bandwidth limit = Bandwidth.classic(10, Refill.greedy(10, Duration.ofMinutes(1)));
        return Bucket.builder().addLimit(limit).build();
    }

    /** 5 intentos por minuto con recarga gradual */
    private Bucket buildRegisterBucket() {
        Bandwidth limit = Bandwidth.classic(5, Refill.greedy(5, Duration.ofMinutes(1)));
        return Bucket.builder().addLimit(limit).build();
    }

    /** Resuelve la IP real del cliente, considerando proxies (X-Forwarded-For). */
    private String resolveClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            // El primer elemento es la IP original del cliente
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private void sendTooManyRequests(HttpServletResponse response) throws IOException {
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(
            "{\"error\":\"Too Many Requests\",\"message\":\"Has excedido el límite de intentos. Intenta de nuevo en un minuto.\"}"
        );
    }
}
