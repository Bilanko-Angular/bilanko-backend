package com.backend.bilanko.DTO.person;

import com.backend.bilanko.models.person.Role;

public record UserResponse(
        long id,
        String name,
        String subname,
        String email,
        Role role,
        String profilePictureUrl,
        String phoneNumber,
        String companyName,
        NotificationPreferencesDTO notificationPreferences,
        AppearancePreferencesDTO appearancePreferences
) {}