package com.foodapp.foodhub.exceptions;

public class OutsideDeliveryZoneException extends RuntimeException {
    public OutsideDeliveryZoneException() {
        super("Delivery address is outside the restaurant's zone");
    }
}


