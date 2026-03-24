package com.edidev.academyApp.enums;

public enum StudentStatus {
    ACTIVE("Activo"),
    INACTIVE("Inactivo"),
    SUSPENDED("Suspendido"),
    GRADUATED("Graduado"),
    DROPPED_OUT("Retirado"),
    ON_HOLD("En pausa");

    private final String displayName;

    StudentStatus(String displayName) {
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