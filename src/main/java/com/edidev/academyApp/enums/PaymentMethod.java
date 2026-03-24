package com.edidev.academyApp.enums;

public enum PaymentMethod {
    CASH("Efectivo"),
    CREDIT_CARD("Tarjeta de Crédito"),
    DEBIT_CARD("Tarjeta de Débito"),
    BANK_TRANSFER("Transferencia Bancaria"),
    MOBILE_PAYMENT("Pago Móvil");

    private final String displayName;

    PaymentMethod(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}