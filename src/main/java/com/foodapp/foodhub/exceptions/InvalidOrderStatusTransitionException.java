package com.foodapp.foodhub.exceptions;

public class InvalidOrderStatusTransitionException extends RuntimeException {
    public InvalidOrderStatusTransitionException(String message) {
        super(message);
    }
    
    public InvalidOrderStatusTransitionException() {
        super("Invalid status transition");
    }
}


