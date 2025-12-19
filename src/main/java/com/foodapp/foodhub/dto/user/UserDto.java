package com.foodapp.foodhub.dto.user;

import com.foodapp.foodhub.dto.restaurant.RestaurantDto;
import lombok.Data;

import java.util.Set;
@Data
public class UserDto
{
    private String fullName;
    private String email;
    private String phone;
    private Set<RestaurantDto> restaurants;
}
