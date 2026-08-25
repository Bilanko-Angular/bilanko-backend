package com.backend.bilanko.models.person;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "password_reset_otp")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PasswordResetOtp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Adresse e-mail de l'utilisateur */
    @Column(nullable = false)
    private String email;

    /** Code OTP à 6 chiffres */
    @Column(nullable = false, length = 6)
    private String otpCode;

    /** Nombre de tentatives échouées */
    @Column(nullable = false)
    @Builder.Default
    private int attempts = 0;

    /** Date d'expiration de l'OTP (5 minutes après génération) */
    @Column(nullable = false)
    private LocalDateTime expiresAt;

    /** Indique si l'OTP a été validé avec succès */
    @Column(nullable = false)
    @Builder.Default
    private boolean verified = false;

    /** Date de création */
    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    public boolean hasExceededMaxAttempts() {
        return attempts >= 5;
    }
}
