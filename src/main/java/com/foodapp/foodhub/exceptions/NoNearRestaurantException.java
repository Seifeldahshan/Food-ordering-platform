package com.foodapp.foodhub.exceptions;

public class NoNearRestaurantException extends RuntimeException {
    public NoNearRestaurantException()
    {
        super("You have no meals in your Restaurant ");
    }
}
