package com.edidev.academyApp.enums;

public enum ClassStatus {
    SCHEDULED("Programada"),
    IN_PROGRESS("En Progreso"),
    COMPLETED("Completada"),
    CANCELLED("Cancelada"),
    POSTPONED("Pospuesta"),
    NO_SHOW("Sin Presentarse");

    private final String displayName;

    ClassStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}