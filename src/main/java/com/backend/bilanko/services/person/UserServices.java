package com.backend.bilanko.services.person;

import com.backend.bilanko.DTO.person.AppearancePreferencesDTO;
import com.backend.bilanko.DTO.person.ChangePasswordRequest;
import com.backend.bilanko.DTO.person.NotificationPreferencesDTO;
import com.backend.bilanko.DTO.person.UpdateProfileRequest;
import com.backend.bilanko.models.person.AppearancePreferences;
import com.backend.bilanko.models.person.NotificationPreferences;
import com.backend.bilanko.models.person.Role;
import com.backend.bilanko.models.person.User;
import org.springframework.web.multipart.MultipartFile;

public interface UserServices {
    User findUserByEmail(String email);
    boolean confirmRoleByEmail(Role role, String email);

    User updateProfile(String email, UpdateProfileRequest request);
    void deleteProfilePicture(String email);
    void changePassword(String email, ChangePasswordRequest request);

    NotificationPreferences getNotificationPreferences(String email);
    NotificationPreferences updateNotificationPreferences(String email, NotificationPreferencesDTO dto);

    AppearancePreferences getAppearancePreferences(String email);
    AppearancePreferences updateAppearancePreferences(String email, AppearancePreferencesDTO dto);

    User updateProfilePicture(String email, MultipartFile file);
    void logoutAllDevices(String email);

}
