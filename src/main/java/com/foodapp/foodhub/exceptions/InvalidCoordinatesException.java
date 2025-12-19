package com.foodapp.foodhub.exceptions;

public class InvalidCoordinatesException extends RuntimeException {
    public InvalidCoordinatesException(String message) {
        super(message);
    }
    
    public InvalidCoordinatesException() {
        super("Invalid coordinates");
    }
}


