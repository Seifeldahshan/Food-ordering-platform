package com.foodapp.foodhub.dto.cart;

import lombok.*;
@Setter
@Getter
public class AddCartItemRequestDTO {
    private Long userId;
    private Long mealId;
    private int quantity;
}