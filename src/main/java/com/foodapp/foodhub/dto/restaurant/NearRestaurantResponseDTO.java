package com.foodapp.foodhub.dto.restaurant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NearRestaurantResponseDTO
{
    public String name;
    public String imageUrl;
    public String address;
    public Double rating;
}
