package com.foodapp.foodhub.exceptions;

public class NoMealsException extends RuntimeException {
    public NoMealsException()
    {
        super("You have no meals in your Restaurant ");
    }
}
