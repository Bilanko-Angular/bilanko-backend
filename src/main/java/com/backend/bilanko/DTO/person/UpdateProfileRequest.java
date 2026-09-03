package com.backend.bilanko.DTO.person;

import jakarta.validation.constraints.NotBlank;

public record UpdateProfileRequest(
        @NotBlank String name,
        String subname,
        String phoneNumber,
        String companyName
) {}