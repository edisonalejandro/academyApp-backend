package com.edidev.academyApp.enums;

public enum PricingType {
    SINGLE_CLASS("Clase Individual"),
    PACKAGE_4("Paquete 4 Clases"),
    PACKAGE_8("Paquete 8 Clases"),
    PACKAGE_12("Paquete 12 Clases"),
    COUPLE_PACKAGE_8("Paquete Pareja 8 Clases"),
    UNLIMITED_MONTHLY("Mensualidad Ilimitada");

    private final String displayName;

    PricingType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}