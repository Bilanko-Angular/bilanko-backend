package com.backend.bilanko.models.person;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

import com.backend.bilanko.models.BaseEntity;

@Entity
@Table(name = "password_reset_otp")
@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PasswordResetOtp extends BaseEntity {

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

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    public boolean hasExceededMaxAttempts() {
        return attempts >= 5;
    }
}
