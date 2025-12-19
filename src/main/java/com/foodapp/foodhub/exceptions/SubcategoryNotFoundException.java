package com.foodapp.foodhub.exceptions;

public class SubcategoryNotFoundException extends RuntimeException {
    public SubcategoryNotFoundException() {
        super("Subcategory not found.");
    }
}
