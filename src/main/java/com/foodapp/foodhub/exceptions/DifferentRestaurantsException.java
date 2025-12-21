package com.foodapp.foodhub.exceptions;

public class DifferentRestaurantsException extends RuntimeException {
    public DifferentRestaurantsException( ) {
        super("You cannot add meals from different restaurants in one cart.");
    }
}
