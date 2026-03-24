package com.edidev.academyApp.enums;

public enum DanceType {
    SALSA("Salsa"),
    BACHATA("Bachata"),
    MERENGUE("Merengue"),
    REGGAETON("Reggaeton"),
    CUMBIA("Cumbia"),
    TANGO("Tango"),
    KIZOMBA("Kizomba"),
    ZOUK("Zouk"),
    MAMBO("Mambo"),
    CHA_CHA_CHA("Cha Cha Cha");

    private final String displayName;

    DanceType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}