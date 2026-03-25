package com.edidev.academyApp.controller;

import com.edidev.academyApp.dto.DashboardSummaryDTO;
import com.edidev.academyApp.service.DashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Controller para el Dashboard de administración
 * Proporciona resúmenes y estadísticas generales del sistema
 */
@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
@Slf4j
public class DashboardController {

    private final DashboardService dashboardService;

    /**
     * Obtiene el resumen completo del dashboard
     * Solo accesible para ADMIN
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DashboardSummaryDTO> getDashboardSummary() {
        log.info("📊 Obteniendo resumen del dashboard");
        DashboardSummaryDTO summary = dashboardService.getDashboardSummary();
        return ResponseEntity.ok(summary);
    }

    /**
     * Obtiene estadísticas rápidas del sistema
     */
    @GetMapping("/quick-stats")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<DashboardSummaryDTO> getQuickStats() {
        log.info("📈 Obteniendo estadísticas rápidas");
        DashboardSummaryDTO stats = dashboardService.getQuickStats();
        return ResponseEntity.ok(stats);
    }
}
