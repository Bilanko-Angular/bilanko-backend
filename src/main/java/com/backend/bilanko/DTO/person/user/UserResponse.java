package com.backend.bilanko.DTO.person.user;

import com.backend.bilanko.DTO.person.notification.NotificationPreferencesDTO;
import com.backend.bilanko.DTO.person.preference.AppearancePreferencesDTO;
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
        String activite,
        String niu,
        String adresse,
        String dateDeCreationActivite,
        NotificationPreferencesDTO notificationPreferences,
        AppearancePreferencesDTO appearancePreferences
) {}
