package com.backend.bilanko.services.auth;

import com.backend.bilanko.DTO.auth.AuthResponse;
import com.backend.bilanko.DTO.auth.LoginRequest;
import com.backend.bilanko.DTO.auth.RegisterRequest;
import com.backend.bilanko.models.person.Role;
import com.backend.bilanko.models.person.User;
import com.backend.bilanko.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JWTServices jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email déjà utilisé");
        }

        User user = User.builder()
                .name(request.name())
                .subname(request.subname())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role(Role.MERCHANT)
                .build();

        userRepository.save(user);

        String token = jwtService.generateToken(user);
        return new AuthResponse(token);
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur introuvable"));

        String token = jwtService.generateToken(user);
        return new AuthResponse(token);
    }
}
