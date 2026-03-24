package com.edidev.academyApp.enums;

public enum CourseStatus {
    PLANNING("En planificación"),
    OPEN_FOR_ENROLLMENT("Abierto para inscripciones"),
    IN_PROGRESS("En progreso"),
    COMPLETED("Completado"),
    CANCELLED("Cancelado"),
    SUSPENDED("Suspendido");

    private final String displayName;

    CourseStatus(String displayName) {
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