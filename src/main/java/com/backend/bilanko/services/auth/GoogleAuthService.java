package com.backend.bilanko.services.auth;

import com.backend.bilanko.DTO.auth.AuthResponse;
import com.backend.bilanko.models.person.Role;
import com.backend.bilanko.models.person.User;
import com.backend.bilanko.repository.UserRepository;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.UUID;

@Service
public class GoogleAuthService {

    private final GoogleIdTokenVerifier verifier;
    private final UserRepository userRepository;
    private final JWTServices jwtServices;

    public GoogleAuthService(UserRepository userRepository, JWTServices jwtServices,
                             @Value("${GOOGLE_CLIENT}") String googleClientId) {
        this.userRepository = userRepository;
        this.jwtServices = jwtServices;
        this.verifier = new GoogleIdTokenVerifier.Builder(
                new NetHttpTransport(), GsonFactory.getDefaultInstance())
                .setAudience(Collections.singletonList(googleClientId))
                .build();
    }

    public AuthResponse authenticate(String idTokenString) {
        try {
            GoogleIdToken idToken = verifier.verify(idTokenString);
            if (idToken == null) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token Google invalide");
            }

            GoogleIdToken.Payload payload = idToken.getPayload();
            String email = payload.getEmail();
            String name = (String) payload.get("given_name");
            String subname = (String) payload.get("family_name");
            String pictureUrl = (String) payload.get("picture"); // ← la photo de profil

            User user = userRepository.findByEmail(email)
                    .orElseGet(() -> createGoogleUser(email, name, subname, pictureUrl));

            // Si l'utilisateur existait déjà mais n'avait pas encore de photo, on la met à jour
            if (user.getProfilePictureUrl() == null && pictureUrl != null) {
                user.setProfilePictureUrl(pictureUrl);
                userRepository.save(user);
            }

            String jwt = jwtServices.generateToken(user);
            return new AuthResponse(jwt);

        } catch (GeneralSecurityException | IOException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Vérification du token Google échouée");
        }
    }

    private User createGoogleUser(String email, String name, String subname, String pictureUrl) {
        User user = User.builder()
                .email(email)
                .name(name)
                .subname(subname)
                .profilePictureUrl(pictureUrl)
                .password(UUID.randomUUID().toString()) // mot de passe aléatoire, jamais utilisé
                .role(Role.MERCHANT)
                .build();
        return userRepository.save(user);
    }
}
