package com.foodapp.foodhub.dto.restaurant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MealDto {
    private String name;
    private String description;
    private BigDecimal price;
    private Integer availableQuantity;
    private Boolean isAvailable;
    private String imageUrl;
    private Integer stockQuantity;
    private Long restaurantId;
}
