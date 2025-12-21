package com.foodapp.foodhub.exceptions;

public class CartItemNotFoundException extends RuntimeException {
    public CartItemNotFoundException() {
        super("Item not found in cart");
    }
}


