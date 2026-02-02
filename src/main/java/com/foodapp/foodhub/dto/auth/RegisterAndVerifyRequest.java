
package com.foodapp.foodhub.dto.auth;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterAndVerifyRequest {
    @Valid
    private RegisterRequest registerRequest;

    @Valid
    private VerifyRequest verifyRequest;
}
