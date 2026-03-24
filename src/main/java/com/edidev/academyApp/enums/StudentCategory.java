package com.edidev.academyApp.enums;

public enum StudentCategory {
    REGULAR("Regular"),
    UNIVERSITY("Universitario"),
    COUPLE("Pareja"),
    SENIOR("Adulto Mayor"),
    CHILD("Niño");

    private final String displayName;

    StudentCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}