package com.backend.bilanko.controller.person;

import com.backend.bilanko.DTO.person.*;
import com.backend.bilanko.mapper.UserMapper;
import com.backend.bilanko.models.person.NotificationPreferences;
import com.backend.bilanko.models.person.User;
import com.backend.bilanko.services.person.UserServices;
import com.backend.bilanko.utils.routes.UserApiRoutes;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Objects;

@RestController
@RequestMapping(UserApiRoutes.BASE)
@RequiredArgsConstructor
public class UserController {

    private final UserServices userServices;

    @GetMapping(UserApiRoutes.ME)
    public ResponseEntity<UserResponse> getCurrentUser() {
        User user = userServices.findUserByEmail(currentUserEmail());
        return ResponseEntity.ok(UserMapper.toResponse(user));
    }

    @PutMapping(UserApiRoutes.ME)
    public ResponseEntity<UserResponse> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        User user = userServices.updateProfile(currentUserEmail(), request);
        return ResponseEntity.ok(UserMapper.toResponse(user));
    }

    @PutMapping(UserApiRoutes.CHANGE_PASSWORD)
    public ResponseEntity<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        userServices.changePassword(currentUserEmail(), request);
        return ResponseEntity.noContent().build();
    }

    @GetMapping(UserApiRoutes.NOTIFICATIONS)
    public ResponseEntity<NotificationPreferencesDTO> getNotifications() {
        NotificationPreferences prefs = userServices.getNotificationPreferences(currentUserEmail());
        return ResponseEntity.ok(UserMapper.toDtoNotificationPreferenceDTO(prefs));
    }

    @PutMapping(UserApiRoutes.NOTIFICATIONS)
    public ResponseEntity<NotificationPreferencesDTO> updateNotifications(
            @RequestBody NotificationPreferencesDTO request) {
        NotificationPreferences prefs = userServices.updateNotificationPreferences(currentUserEmail(), request);
        return ResponseEntity.ok(UserMapper.toDtoNotificationPreferenceDTO(prefs));
    }

    // --- Utilitaires privés ---

    private String currentUserEmail() {
        return Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getName();
    }


    @PostMapping(value = UserApiRoutes.PHOTO, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UserResponse> uploadPhoto(@RequestParam("file") MultipartFile file) {
        User user = userServices.updateProfilePicture(currentUserEmail(), file);
        return ResponseEntity.ok(UserMapper.toResponse(user));
    }

    @GetMapping(UserApiRoutes.APPEARANCE)
    public ResponseEntity<AppearancePreferencesDTO> getAppearance() {
        return ResponseEntity.ok(UserMapper.toAppearancePreferenceDTO(userServices.getAppearancePreferences(currentUserEmail())));
    }

    @PutMapping(UserApiRoutes.APPEARANCE)
    public ResponseEntity<AppearancePreferencesDTO> updateAppearance(@RequestBody AppearancePreferencesDTO request) {
        return ResponseEntity.ok(UserMapper.toAppearancePreferenceDTO(userServices.updateAppearancePreferences(currentUserEmail(), request)));
    }

    @PostMapping(UserApiRoutes.LOGOUT_ALL)
    public ResponseEntity<Void> logoutAllDevices() {
        userServices.logoutAllDevices(currentUserEmail());
        return ResponseEntity.noContent().build();
    }
}