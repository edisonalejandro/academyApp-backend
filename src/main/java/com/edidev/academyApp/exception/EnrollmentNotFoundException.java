package com.edidev.academyApp.exception;

public class EnrollmentNotFoundException extends RuntimeException {
    public EnrollmentNotFoundException(String message) {
        super(message);
    }
}