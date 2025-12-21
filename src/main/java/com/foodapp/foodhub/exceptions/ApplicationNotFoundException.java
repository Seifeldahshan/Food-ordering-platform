package com.foodapp.foodhub.exceptions;

public class ApplicationNotFoundException extends RuntimeException {
    public ApplicationNotFoundException() {
        super("Application not found.");
    }
}
