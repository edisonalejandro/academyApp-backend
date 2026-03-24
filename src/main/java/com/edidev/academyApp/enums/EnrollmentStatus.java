package com.edidev.academyApp.enums;

public enum EnrollmentStatus {
    PENDING("Pendiente"),
    ACTIVE("Activa"),
    COMPLETED("Completada"),
    CANCELLED("Cancelada"),
    SUSPENDED("Suspendida"),
    TRANSFERRED("Transferida"),
    HOURS_EXHAUSTED("Horas Agotadas");

    private final String displayName;

    EnrollmentStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}