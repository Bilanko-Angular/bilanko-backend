package com.backend.bilanko.services.person;

import com.backend.bilanko.DTO.person.ChangePasswordRequest;
import com.backend.bilanko.DTO.person.NotificationPreferencesDTO;
import com.backend.bilanko.DTO.person.UpdateProfileRequest;
import com.backend.bilanko.models.person.NotificationPreferences;
import com.backend.bilanko.models.person.Role;
import com.backend.bilanko.models.person.User;
import com.backend.bilanko.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@AllArgsConstructor
public class UserServicesImpl implements UserServices {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder; // déjà utilisé au signup, à réinjecter ici

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
    public void changePassword(String email, ChangePasswordRequest request) {
        User user = findUserByEmail(email);
        // On vérifie l'ancien mot de passe avant d'autoriser le changement
        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mot de passe actuel incorrect");
        }
        user.setPassword(passwordEncoder.encode(request.newPassword()));
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
}