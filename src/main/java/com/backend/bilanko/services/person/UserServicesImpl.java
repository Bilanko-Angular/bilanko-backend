package com.backend.bilanko.services.person;

import com.backend.bilanko.DTO.person.AppearancePreferencesDTO;
import com.backend.bilanko.DTO.person.ChangePasswordRequest;
import com.backend.bilanko.DTO.person.NotificationPreferencesDTO;
import com.backend.bilanko.DTO.person.UpdateProfileRequest;
import com.backend.bilanko.models.person.AppearancePreferences;
import com.backend.bilanko.models.person.NotificationPreferences;
import com.backend.bilanko.models.person.Role;
import com.backend.bilanko.models.person.User;
import com.backend.bilanko.repository.UserRepository;
import com.backend.bilanko.services.external.FileStorageService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Service
@AllArgsConstructor
public class UserServicesImpl implements UserServices {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder; // déjà utilisé au signup, à réinjecter ici
    private final FileStorageService fileStorageService;

    @Override
    public User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Utilisateur introuvable : " + email));
    }

    @Override
    public boolean confirmRoleByEmail(Role role, String email) {
        User user = findUserByEmail(email);
        return user.getRole() == role;
    }

    @Override
    public User updateProfile(String email, UpdateProfileRequest request) {
        User user = findUserByEmail(email);
        user.setName(request.name());
        user.setSubname(request.subname());
        user.setPhoneNumber(request.phoneNumber());
        user.setCompanyName(request.companyName());
        return userRepository.save(user);
    }

    @Override
    public void deleteProfilePicture(String email) {
        User user= findUserByEmail(email);
        user.setProfilePictureUrl(null);
        userRepository.save(user);
    }

    @Override
    public NotificationPreferences getNotificationPreferences(String email) {
        return findUserByEmail(email).getNotificationPreferences();
    }

    @Override
    public NotificationPreferences updateNotificationPreferences(String email, NotificationPreferencesDTO dto) {
        User user = findUserByEmail(email);
        NotificationPreferences prefs = user.getNotificationPreferences();
        prefs.setStockAlerts(dto.stockAlerts());
        prefs.setNewSales(dto.newSales());
        prefs.setMonthlyReports(dto.monthlyReports());
        prefs.setUpdates(dto.updates());
        userRepository.save(user);
        return prefs;
    }
    @Override
    public void changePassword(String email, ChangePasswordRequest request) {
        User user = findUserByEmail(email);
        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mot de passe actuel incorrect");
        }
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        user.setTokenVersion(user.getTokenVersion() + 1); // invalide les autres sessions par sécurité
        userRepository.save(user);
    }

    @Override
    public AppearancePreferences getAppearancePreferences(String email) {
        return findUserByEmail(email).getAppearancePreferences();
    }

    @Override
    public AppearancePreferences updateAppearancePreferences(String email, AppearancePreferencesDTO dto) {
        User user = findUserByEmail(email);
        AppearancePreferences prefs = user.getAppearancePreferences();
        prefs.setTheme(dto.theme());
        prefs.setLanguage(dto.language());
        prefs.setDateFormat(dto.dateFormat());
        prefs.setCurrency(dto.currency());
        prefs.setCompactMode(dto.compactMode());
        userRepository.save(user);
        return prefs;
    }

    @Override
    public User updateProfilePicture(String email, MultipartFile file) {
        User user = findUserByEmail(email);
        String url = fileStorageService.uploadProfilePicture(file, user.getId());
        user.setProfilePictureUrl(url);
        return userRepository.save(user);
    }

    @Override
    public void logoutAllDevices(String email) {
        User user = findUserByEmail(email);
        user.setTokenVersion(user.getTokenVersion() + 1);
        userRepository.save(user);
    }

}