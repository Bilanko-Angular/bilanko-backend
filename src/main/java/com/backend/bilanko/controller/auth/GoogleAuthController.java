package com.backend.bilanko.controller.auth;

import com.backend.bilanko.DTO.auth.AuthResponse;
import com.backend.bilanko.DTO.auth.GoogleLoginDTO;
import com.backend.bilanko.services.auth.GoogleAuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class GoogleAuthController {

    private final GoogleAuthService googleAuthService;

    public GoogleAuthController(GoogleAuthService googleAuthService) {
        this.googleAuthService = googleAuthService;
    }

    @PostMapping("/google")
    public ResponseEntity<AuthResponse> googleLogin(@RequestBody GoogleLoginDTO dto) {
        AuthResponse response = googleAuthService.authenticate(dto.idToken());
        return ResponseEntity.ok(response);
    }
}