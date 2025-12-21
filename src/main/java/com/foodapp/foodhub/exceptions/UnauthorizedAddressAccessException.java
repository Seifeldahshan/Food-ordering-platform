package com.foodapp.foodhub.exceptions;

public class UnauthorizedAddressAccessException extends RuntimeException {
    public UnauthorizedAddressAccessException() {
        super("Address not yours");
    }
}


