package com.foodapp.foodhub.repository;

import com.foodapp.foodhub.dto.restaurant.RestaurantDto;
import com.foodapp.foodhub.entity.Restaurant;
import com.foodapp.foodhub.entity.User;
import com.foodapp.foodhub.enums.RestaurantStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {

    Restaurant findRestaurantByOwner(User owner);

    List<Restaurant> findByStatus(RestaurantStatus status);
}