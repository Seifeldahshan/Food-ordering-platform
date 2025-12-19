package com.foodapp.foodhub.service;
import com.foodapp.foodhub.dto.UserLocationRequest;
import com.foodapp.foodhub.dto.restaurant.NearRestaurantResponseDTO;
import com.foodapp.foodhub.dto.restaurant.RestaurantDto;
import com.foodapp.foodhub.dto.restaurantApplication.RestaurantApplicationDto;
import com.foodapp.foodhub.dto.user.UserDto;
import com.foodapp.foodhub.entity.Restaurant;
import com.foodapp.foodhub.entity.RestaurantApplication;
import com.foodapp.foodhub.entity.User;
import com.foodapp.foodhub.enums.RestaurantStatus;
import com.foodapp.foodhub.enums.UserStatus;
import com.foodapp.foodhub.exceptions.ApplicationNotFoundException;
import com.foodapp.foodhub.exceptions.NoNearRestaurantException;
import com.foodapp.foodhub.mappers.RestaurantApplicationMapper;
import com.foodapp.foodhub.mappers.UserMapper;
import com.foodapp.foodhub.repository.RestaurantApplicationRepository;
import com.foodapp.foodhub.repository.RestaurantRepository;
import com.foodapp.foodhub.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService
{
  private final UserRepository userRepository;
  private final UserMapper userMapper;
  private final RestaurantApplicationMapper restaurantApplicationMapper;
  private final RestaurantApplicationRepository restaurantApplicationRepository;
  private final ZoneService zoneService;
  private final RestaurantRepository restaurantRepository;

   public User findByUsername(String username)
   {
       return userRepository.findByUsername(username);
   }

   public List<UserDto> getAllUsers()
   {
       return userRepository.findAll().stream().map(userMapper::mapToDto).toList();
   }
    public RestaurantApplicationDto getMyApplication(User user)
    {
        RestaurantApplication app = restaurantApplicationRepository.findByOwner(user);
        if (app==null)
            throw new ApplicationNotFoundException();
        return restaurantApplicationMapper.appToDto(app);
    }

    @Transactional
    public void deleteUserById(Long userId)
    {
        userRepository.deleteUserById(userId);
    }
    public void disabledUserById(@PathVariable Long userId)
    {
        User user = userRepository.findById(userId).get();
        user.setStatus(UserStatus.DISABLED);
    }

    public List<NearRestaurantResponseDTO> restaurantsNearMe(UserLocationRequest request)
    {
        List<Restaurant> allRestaurants = restaurantRepository.findByStatus(RestaurantStatus.ACTIVE);
        List<NearRestaurantResponseDTO> nearRestaurants = allRestaurants.stream()
                .filter(restaurant -> zoneService.isWithinDeliveryZone(
                        request.getCustomerLat(),
                        request.getCustomerLon(),
                        restaurant.getLatitude(),
                        restaurant.getLongitude(),
                        restaurant.getDeliveryRadiusInKm()
                ))
                .map(restaurant -> NearRestaurantResponseDTO.builder()
                        .name(restaurant.getName())
                        .imageUrl(restaurant.getImageUrl())
                        .address(restaurant.getAddress())
                        .rating(restaurant.getRating())
                        .build())
                .collect(Collectors.toList());

        if (nearRestaurants.isEmpty())
            throw new NoNearRestaurantException();
        return nearRestaurants;
    }

}
