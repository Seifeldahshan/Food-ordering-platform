package com.foodapp.foodhub.controller;

import com.foodapp.foodhub.dto.user.AddressRequestDto;
import com.foodapp.foodhub.dto.user.AddressResponseDto;
import com.foodapp.foodhub.service.UserAddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/address")
@RequiredArgsConstructor
public class UserAddressController {

    private final UserAddressService addressService;

    @PostMapping("/{userId}")
    public ResponseEntity<AddressResponseDto> addAddress(
            @PathVariable Long userId,
            @RequestBody AddressRequestDto dto) {

        AddressResponseDto response = addressService.addAddress(userId, dto);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<AddressResponseDto> getUserAddresses(@PathVariable Long userId) {
        return ResponseEntity.ok(addressService.getLatestAddress(userId));
    }

    @GetMapping("/user/{userId}/latest")
    public ResponseEntity<AddressResponseDto> getLatestAddress(@PathVariable Long userId) {
        return ResponseEntity.ok(addressService.getLatestAddress(userId));
    }

    @DeleteMapping("/{userId}/{addressId}")
    public ResponseEntity<String> deleteAddress(@PathVariable Long userId, @PathVariable Long addressId) {
        addressService.deleteAddress(userId, addressId);
        return ResponseEntity.ok("Address deleted");
    }

    @PutMapping("/{userId}/{addressId}")
    public ResponseEntity<AddressResponseDto> updateAddress(
            @PathVariable Long userId,
            @PathVariable Long addressId,
            @RequestBody AddressRequestDto dto) {

        return ResponseEntity.ok(addressService.updateAddress(userId,addressId,dto));
    }
}
