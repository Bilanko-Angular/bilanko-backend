package com.backend.bilanko.DTO.person;

import lombok.Builder;

@Builder
public record UserResponseDTO(
        String name,
        String subname,
        String email,
        String profilePicture
) {
}
