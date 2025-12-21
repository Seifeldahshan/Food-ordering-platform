package com.foodapp.foodhub.service;

import com.foodapp.foodhub.dto.restaurant.AddMealRequest;
import com.foodapp.foodhub.dto.restaurant.MealDto;
import com.foodapp.foodhub.dto.restaurant.UpdateMealPriceRequest;
import com.foodapp.foodhub.entity.Meal;
import com.foodapp.foodhub.entity.User;
import com.foodapp.foodhub.exceptions.NoMealsException;
import com.foodapp.foodhub.mappers.MealMapper;
import com.foodapp.foodhub.repository.MealRepository;
import com.foodapp.foodhub.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OwnerService
{
    private final MealRepository mealRepository;
    private final RestaurantRepository restaurantRepository;
    private final MealMapper mealMapper;
    @Transactional
    public MealDto addMeal(User owner , AddMealRequest request)
    {
        Meal meal = Meal.builder()
                .restaurant(restaurantRepository.findRestaurantByOwner(owner))
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .imageUrl(request.getImageUrl())
                .availableQuantity(request.getAvailableQuantity())
                .stockQuantity(request.getStockQuantity())
                .isAvailable(request.getAvailableQuantity() != null && request.getAvailableQuantity() > 0)
                .build();
        meal = mealRepository.save(meal);
        return mealMapper.toDto(meal);
    }

    public void deleteMeal (Long mealId)
    {
        mealRepository.deleteById(mealId);
    }

    public List<MealDto> getAllMeals (User owner)
    {
        if (mealRepository.findByRestaurantId(restaurantRepository.findRestaurantByOwner(owner).getId()) == null)
            throw new NoMealsException();
        return mealRepository.findByRestaurantId(restaurantRepository.findRestaurantByOwner(owner).getId())
                .stream().map(mealMapper::toDto).toList();
    }

    public MealDto getMealById(Long mealId)
    {

        return mealRepository.findById(mealId).
                map(mealMapper::toDto).orElseThrow(() -> new ResourceNotFoundException("Meal not found"));
    }

    public MealDto updateMealPrice (UpdateMealPriceRequest request, Long mealId)
    {
        Meal meal = mealRepository.findById(mealId).get();
        meal.setPrice(request.getPrice());
        return mealMapper.toDto(meal);
    }
}
