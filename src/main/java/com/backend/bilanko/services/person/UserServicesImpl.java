package com.backend.bilanko.services.person;

import com.backend.bilanko.models.person.Role;
import com.backend.bilanko.models.person.User;
import com.backend.bilanko.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@AllArgsConstructor
public class UserServicesImpl implements UserServices {
    private final UserRepository userRepository;
    public User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Utilisateur introuvable : " + email));
    }

    @Override
    public boolean confirmRoleByEmail(Role role, String email) {
        User user=findUserByEmail(email);
        return user.getRole()==role;
    }
}
