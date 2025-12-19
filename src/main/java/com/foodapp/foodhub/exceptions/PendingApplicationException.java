package com.foodapp.foodhub.exceptions;

public class PendingApplicationException extends RuntimeException {
    public PendingApplicationException() {
        super("You have already submitted an application. Wait for Admin Response");
    }
}
