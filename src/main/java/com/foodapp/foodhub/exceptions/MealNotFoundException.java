package com.foodapp.foodhub.exceptions;

public class MealNotFoundException extends RuntimeException {
    public MealNotFoundException() {
        super("Meal not found");
    }
}
