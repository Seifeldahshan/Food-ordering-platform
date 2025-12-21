package com.foodapp.foodhub.exceptions;

public class AddressNotFoundException extends RuntimeException {
    public AddressNotFoundException() {
        super("Address not found");
    }
}


