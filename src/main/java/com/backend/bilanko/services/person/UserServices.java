package com.backend.bilanko.services.person;

import com.backend.bilanko.DTO.person.ChangePasswordRequest;
import com.backend.bilanko.DTO.person.NotificationPreferencesDTO;
import com.backend.bilanko.DTO.person.UpdateProfileRequest;
import com.backend.bilanko.models.person.NotificationPreferences;
import com.backend.bilanko.models.person.Role;
import com.backend.bilanko.models.person.User;

public interface UserServices {
    User findUserByEmail(String email);
    boolean confirmRoleByEmail(Role role, String email);

    User updateProfile(String email, UpdateProfileRequest request);
    void changePassword(String email, ChangePasswordRequest request);
    NotificationPreferences getNotificationPreferences(String email);
    NotificationPreferences updateNotificationPreferences(String email, NotificationPreferencesDTO dto);
}
