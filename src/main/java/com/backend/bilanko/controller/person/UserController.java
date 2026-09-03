package com.backend.bilanko.controller.person;

import com.backend.bilanko.DTO.person.*;
import com.backend.bilanko.models.person.NotificationPreferences;
import com.backend.bilanko.models.person.User;
import com.backend.bilanko.services.person.UserServices;
import com.backend.bilanko.utils.routes.UserApiRoutes;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

@RestController
@RequestMapping(UserApiRoutes.BASE)
@RequiredArgsConstructor
public class UserController {

    private final UserServices userServices;

    @GetMapping(UserApiRoutes.ME)
    public ResponseEntity<UserResponse> getCurrentUser() {
        User user = userServices.findUserByEmail(currentUserEmail());
        return ResponseEntity.ok(toResponse(user));
    }

    @PutMapping(UserApiRoutes.ME)
    public ResponseEntity<UserResponse> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        User user = userServices.updateProfile(currentUserEmail(), request);
        return ResponseEntity.ok(toResponse(user));
    }

    @PutMapping(UserApiRoutes.CHANGE_PASSWORD)
    public ResponseEntity<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        userServices.changePassword(currentUserEmail(), request);
        return ResponseEntity.noContent().build();
    }

    @GetMapping(UserApiRoutes.NOTIFICATIONS)
    public ResponseEntity<NotificationPreferencesDTO> getNotifications() {
        NotificationPreferences prefs = userServices.getNotificationPreferences(currentUserEmail());
        return ResponseEntity.ok(toDto(prefs));
    }

    @PutMapping(UserApiRoutes.NOTIFICATIONS)
    public ResponseEntity<NotificationPreferencesDTO> updateNotifications(
            @RequestBody NotificationPreferencesDTO request) {
        NotificationPreferences prefs = userServices.updateNotificationPreferences(currentUserEmail(), request);
        return ResponseEntity.ok(toDto(prefs));
    }

    // --- Utilitaires privés ---

    private String currentUserEmail() {
        return Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getName();
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(), user.getName(), user.getSubname(), user.getEmail(),
                user.getRole(), user.getProfilePictureUrl(), user.getPhoneNumber(),
                user.getCompanyName(), toDto(user.getNotificationPreferences())
        );
    }

    private NotificationPreferencesDTO toDto(NotificationPreferences prefs) {
        return new NotificationPreferencesDTO(
                prefs.isStockAlerts(), prefs.isNewSales(), prefs.isMonthlyReports(), prefs.isUpdates()
        );
    }
}