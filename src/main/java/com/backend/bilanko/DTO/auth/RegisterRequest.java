package com.backend.bilanko.DTO.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank String name,
        @NotBlank String subname,
        @Email @NotBlank String email,
        @Size(min = 6) String password
) {}
