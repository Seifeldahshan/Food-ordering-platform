package com.foodapp.foodhub.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class VerifyRequest {

    @Email
    @NotNull(message = "email cant be Null!")
    private String email;

    @NotNull(message = "code cant be Null!")
    @Size(min = 4, max = 4)
    private String code;
}