package com.backend.bilanko.mapper;

import com.backend.bilanko.DTO.person.AppearancePreferencesDTO;
import com.backend.bilanko.DTO.person.NotificationPreferencesDTO;
import com.backend.bilanko.DTO.person.UserResponse;
import com.backend.bilanko.DTO.person.UserResponseDTO;
import com.backend.bilanko.models.person.AppearancePreferences;
import com.backend.bilanko.models.person.NotificationPreferences;
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

    public static AppearancePreferencesDTO toAppearancePreferenceDTO(AppearancePreferences prefs) {
        return new AppearancePreferencesDTO(
                prefs.getTheme(), prefs.getLanguage(), prefs.getDateFormat(),
                prefs.getCurrency(), prefs.isCompactMode()
        );
    }

    public static UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(), user.getName(), user.getSubname(), user.getEmail(),
                user.getRole(), user.getProfilePictureUrl(), user.getPhoneNumber(),
                user.getCompanyName(), toDtoNotificationPreferenceDTO(user.getNotificationPreferences()),
                toAppearancePreferenceDTO(user.getAppearancePreferences()) // <-- à ajouter
        );
    }

    public static NotificationPreferencesDTO toDtoNotificationPreferenceDTO(NotificationPreferences prefs) {
        return new NotificationPreferencesDTO(
                prefs.isStockAlerts(), prefs.isNewSales(), prefs.isMonthlyReports(), prefs.isUpdates()
        );
    }
}
