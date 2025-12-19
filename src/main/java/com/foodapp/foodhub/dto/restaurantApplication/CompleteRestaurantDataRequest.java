package com.foodapp.foodhub.dto.restaurantApplication;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompleteRestaurantDataRequest
{
    @NotBlank(message = "Description is required.")
    private String description;

    @NotBlank(message = "Address is required.")
    private String address;

    @NotBlank(message = "Latitude is required.")

    private double latitude;

    @NotBlank(message = "Longitude is required.")
    private double longitude;

    private String imageUrl;

    private Long subcategoryId;

}
