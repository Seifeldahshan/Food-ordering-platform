package com.foodapp.foodhub.exceptions;

public class CartNotFoundException extends RuntimeException {
    public CartNotFoundException() {
        super("Cart not found.");
    }
}
