package com.backend.bilanko.DTO.person.user;

import jakarta.validation.constraints.NotBlank;

public record UpdateProfileRequest(
        @NotBlank String name,
        String subname,
        String phoneNumber,
        String companyName,
        String activite,
        String niu,
        String adresse,
        String dateDeCreationActivite
) {}
