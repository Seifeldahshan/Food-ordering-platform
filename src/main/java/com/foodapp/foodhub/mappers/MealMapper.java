package com.foodapp.foodhub.mappers;

import com.foodapp.foodhub.dto.restaurant.AddMealRequest;
import com.foodapp.foodhub.dto.restaurant.MealDto;
import com.foodapp.foodhub.entity.Meal;
import com.foodapp.foodhub.entity.Restaurant;
import org.springframework.stereotype.Component;

@Component
public class MealMapper
{
    public Meal toEntity(AddMealRequest request, Restaurant restaurant)
    {
        return Meal.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .availableQuantity(request.getAvailableQuantity())
                .isAvailable(true)  // default value
                .imageUrl(request.getImageUrl())
                .stockQuantity(request.getStockQuantity())
                .restaurant(restaurant)
                .build();
    }

    public MealDto toDto(Meal meal)
    {
        return MealDto.builder()
                .name(meal.getName())
                .description(meal.getDescription())
                .price(meal.getPrice())
                .availableQuantity(meal.getAvailableQuantity())
                .isAvailable(meal.getIsAvailable())
                .imageUrl(meal.getImageUrl())
                .stockQuantity(meal.getStockQuantity())
                .restaurantId(meal.getRestaurant() != null ? meal.getRestaurant().getId() : null)
                .build();
    }
}
