package com.foodapp.foodhub.repository;

import com.foodapp.foodhub.entity.RestaurantApplication;
import com.foodapp.foodhub.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RestaurantApplicationRepository extends JpaRepository<RestaurantApplication, Long> {
  RestaurantApplication findByOwner(User owner);
}