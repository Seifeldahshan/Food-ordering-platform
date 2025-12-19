package com.foodapp.foodhub.exceptions;

public class AccountIsDisabledException extends RuntimeException {
    public AccountIsDisabledException() {
        super("Your Account is disabled.");
    }
}
