package com.foodapp.foodhub.service;

import com.foodapp.foodhub.entity.*;
import com.foodapp.foodhub.exceptions.CartItemNotFoundException;
import com.foodapp.foodhub.exceptions.CartNotFoundException;
import com.foodapp.foodhub.exceptions.DifferentRestaurantsException;
import com.foodapp.foodhub.exceptions.MealNotFoundException;
import com.foodapp.foodhub.exceptions.UserNotFoundException;
import com.foodapp.foodhub.repository.CartItemRepository;
import com.foodapp.foodhub.repository.CartRepository;
import com.foodapp.foodhub.repository.MealRepository;
import com.foodapp.foodhub.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CartService
{
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final MealRepository mealRepository;
    public Cart addItemToCart(Long userId, Long mealId, int quantity) {

            // Find or create cart
            Cart cart = cartRepository.findByUserId(userId)
                    .orElseGet(() -> {
                        Cart newCart = new Cart();
                        newCart.setUser(userRepository.findById(userId)
                                .orElseThrow(UserNotFoundException::new));
                        return cartRepository.save(newCart);
                    });

            Meal meal = mealRepository.findById(mealId)
                    .orElseThrow(MealNotFoundException::new);

             Restaurant mealRestaurant = meal.getRestaurant();

            if (cart.getRestaurant() == null) {
                cart.setRestaurant(mealRestaurant);
            }

            // 2️⃣ If restaurant does not match → user is mixing restaurants
            if (!cart.getRestaurant().getId().equals(mealRestaurant.getId())) {
                throw new DifferentRestaurantsException();
            }
            // 3️⃣ Check if item already exists in cart
            CartItem existingItem = cart.getItems().stream()
                    .filter(item -> item.getMeal().getId().equals(mealId))
                    .findFirst()
                    .orElse(null);

            if (existingItem != null) {
                // 4️⃣ If exists, update quantity
                existingItem.setQuantity(existingItem.getQuantity() + quantity);
            } else {
                // 5️⃣ Else create new item
                CartItem newItem = new CartItem();
                newItem.setMeal(meal);
                newItem.setMealName(meal.getName());
                newItem.setUnitPrice(meal.getPrice());
                newItem.setQuantity(quantity);
                newItem.setCart(cart);
                cart.getItems().add(newItem);
            }
            // 6️⃣ Save and return updated cart
            return cartRepository.save(cart);
        }

    public String removeItemFromCart(Long userId, Long mealId) {

        // 1️⃣ Find the user's cart
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(CartNotFoundException::new);

        // 2️⃣ Find the item to remove
        CartItem itemToRemove = cart.getItems().stream()
                .filter(item -> item.getMeal().getId().equals(mealId))
                .findFirst()
                .orElseThrow(CartItemNotFoundException::new);

        // 3️⃣ Remove from cart set
        cart.getItems().remove(itemToRemove);

        // 4️⃣ Optional: delete from repository if needed
        cartItemRepository.delete(itemToRemove);

        // 5️⃣ Save cart
       cartRepository.save(cart);
        return "Item removed successfully ";
    }


    public Cart getCart( Long userId){
        return cartRepository.findByUserId(userId)
                .orElseThrow(CartNotFoundException::new);

    }
}

