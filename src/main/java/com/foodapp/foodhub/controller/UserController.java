package com.foodapp.foodhub.controller;
import com.foodapp.foodhub.dto.UserLocationRequest;
import com.foodapp.foodhub.dto.restaurant.NearRestaurantResponseDTO;
import com.foodapp.foodhub.dto.restaurantApplication.RestaurantApplicationDto;
import com.foodapp.foodhub.dto.restaurantApplication.RestaurantApplicationRequest;
import com.foodapp.foodhub.dto.restaurantApplication.RestaurantApplicationResponse;
import com.foodapp.foodhub.dto.user.UserDto;
import com.foodapp.foodhub.entity.User;
import com.foodapp.foodhub.mappers.UserMapper;
import com.foodapp.foodhub.repository.UserRepository;
import com.foodapp.foodhub.service.RestaurantApplicationService;
import com.foodapp.foodhub.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController @RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController
{
    private final RestaurantApplicationService restaurantApplicationService;
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final UserService userService;

    @PostMapping("/createApplication")
    public ResponseEntity<RestaurantApplicationResponse> createApplication(@AuthenticationPrincipal User user,
            @RequestBody @Valid RestaurantApplicationRequest request)
    {
     //   send notification here for user and Admin
        RestaurantApplicationResponse response = restaurantApplicationService.createApplication(user, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/myApplication")
    public ResponseEntity<RestaurantApplicationDto> getMyApplication(@AuthenticationPrincipal User user)
    {
        RestaurantApplicationDto response = userService.getMyApplication(user);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public UserDto me(@AuthenticationPrincipal User user)
    {
        return userRepository.findById(user.getId()).map(userMapper::mapToDto).get();
    }

    @GetMapping ("/nearRestaurant")
    public ResponseEntity<List<NearRestaurantResponseDTO>> restaurantsNearMe(@RequestBody UserLocationRequest request)
    {
        return ResponseEntity.ok(userService.restaurantsNearMe(request));
    }
}
