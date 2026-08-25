package com.backend.bilanko.mapper;

import com.backend.bilanko.DTO.person.UserResponseDTO;
import com.backend.bilanko.models.person.User;

public final class UserMapper {
    public static UserResponseDTO toUserResponseDto(User user){
        return UserResponseDTO.builder()
                .name(user.getName())
                .subname(user.getSubname())
                .email(user.getEmail())
                .profilePicture(user.getProfilePictureUrl())
                .build();
    }
}
