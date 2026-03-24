package com.edidev.academyApp.exception;

public class PricingRuleNotFoundException extends RuntimeException {
    
    public PricingRuleNotFoundException(String message) {
        super(message);
    }
    
    public PricingRuleNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}