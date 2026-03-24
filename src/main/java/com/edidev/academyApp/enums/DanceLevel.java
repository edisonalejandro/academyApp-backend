package com.edidev.academyApp.enums;

public enum DanceLevel {
    BEGINNER("Principiante"),
    INTERMEDIATE("Intermedio"),
    ADVANCED("Avanzado"),
    MASTER("Maestro"),
    OPEN("Nivel Abierto");

    private final String displayName;

    DanceLevel(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}