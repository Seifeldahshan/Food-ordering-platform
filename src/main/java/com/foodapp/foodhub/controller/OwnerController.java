package com.foodapp.foodhub.controller;
import com.foodapp.foodhub.dto.restaurant.AddMealRequest;
import com.foodapp.foodhub.dto.restaurant.MealDto;
import com.foodapp.foodhub.dto.restaurant.UpdateMealPriceRequest;
import com.foodapp.foodhub.dto.restaurantApplication.CompleteRestaurantDataRequest;
import com.foodapp.foodhub.dto.restaurant.RestaurantDto;
import com.foodapp.foodhub.entity.User;
import com.foodapp.foodhub.service.OwnerService;
import com.foodapp.foodhub.service.RestaurantApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController
@RequestMapping("/api/owner")
@RequiredArgsConstructor
public class OwnerController
{
    private final RestaurantApplicationService restaurantService;
    private final OwnerService ownerService;

    @PutMapping("/completeRestaurantData")
    public ResponseEntity<RestaurantDto> completeRestaurantData(@AuthenticationPrincipal User user,
            @Valid @RequestBody CompleteRestaurantDataRequest request)
    {

        RestaurantDto updated = restaurantService.completeRestaurantData(user, request);
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/meal")
    public ResponseEntity<MealDto> addMeal(@AuthenticationPrincipal User owner,
                                                          @Valid @RequestBody AddMealRequest request)
    {
        MealDto mealDto = ownerService.addMeal(owner, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(mealDto);
    }


    @GetMapping("/meal")
    public ResponseEntity<List<MealDto>> getAllMeals(@AuthenticationPrincipal User owner)
    {
        return ResponseEntity.ok(ownerService.getAllMeals(owner));
    }

    @GetMapping("/meal/{mealId}")
    public ResponseEntity<MealDto> getMealById(@PathVariable Long mealId)
    {
        return ResponseEntity.ok(ownerService.getMealById(mealId));
    }

    @PutMapping("/meal/{mealId}")
    public ResponseEntity<MealDto> updateMealPrice(@Valid UpdateMealPriceRequest request, @PathVariable Long mealId)
    {
        return ResponseEntity.ok(ownerService.updateMealPrice(request, mealId));
    }

    @DeleteMapping("/meal/{mealId}")
    public ResponseEntity<Void> deleteMeal(@PathVariable Long mealId)
    {
        ownerService.deleteMeal(mealId);
        return ResponseEntity.noContent().build();
    }

}
