package com.backend.bilanko.controller.auth;

import com.backend.bilanko.DTO.auth.ForgotPasswordRequest;
import com.backend.bilanko.DTO.auth.ForgotPasswordResponse;
import com.backend.bilanko.DTO.auth.ResetPasswordRequest;
import com.backend.bilanko.DTO.auth.VerifyOtpRequest;
import com.backend.bilanko.services.auth.PasswordResetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth/password")
@RequiredArgsConstructor
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    /**
     * Étape 1 : L'utilisateur envoie son e-mail.
     * → On vérifie s'il existe et on envoie un OTP à 6 chiffres par mail.
     */
    @PostMapping("/forgot")
    public ResponseEntity<ForgotPasswordResponse> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request
    ) {
        ForgotPasswordResponse response = passwordResetService.sendOtp(request.email());
        return ResponseEntity.ok(response);
    }

    /**
     * Étape 2 : L'utilisateur entre le code OTP reçu par mail.
     * → Si valide, on renvoie un token de réinitialisation (valide 10 min).
     * → Si 5 tentatives échouées, on lui demande de recommencer.
     */
    @PostMapping("/verify-otp")
    public ResponseEntity<ForgotPasswordResponse> verifyOtp(
            @Valid @RequestBody VerifyOtpRequest request
    ) {
        ForgotPasswordResponse response = passwordResetService.verifyOtp(request.email(), request.otp());
        if (!response.success()) {
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }

    /**
     * Étape 3 : L'utilisateur envoie le token + le nouveau mot de passe.
     * → On vérifie le token et on met à jour le mot de passe.
     */
    @PostMapping("/reset")
    public ResponseEntity<ForgotPasswordResponse> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request
    ) {
        ForgotPasswordResponse response = passwordResetService.resetPassword(request.token(), request.newPassword());
        if (!response.success()) {
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }
}
