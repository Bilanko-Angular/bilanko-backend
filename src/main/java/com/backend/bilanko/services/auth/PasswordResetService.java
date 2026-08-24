package com.backend.bilanko.services.auth;

import com.backend.bilanko.DTO.auth.ForgotPasswordResponse;
import com.backend.bilanko.models.person.PasswordResetOtp;
import com.backend.bilanko.models.person.User;
import com.backend.bilanko.repository.PasswordResetOtpRepository;
import com.backend.bilanko.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordResetOtpRepository otpRepository;
    private final EmailService emailService;
    private final JWTServices jwtService;
    private final PasswordEncoder passwordEncoder;

    private static final int OTP_VALIDITY_MINUTES = 5;
    private static final int MAX_ATTEMPTS = 5;
    private static final long RESET_TOKEN_EXPIRATION_MS = 10 * 60 * 1000; // 10 minutes

    // ────────────────────────────────────────────────────────────────
    // Étape 1 : Vérifier l'email et envoyer l'OTP
    // ────────────────────────────────────────────────────────────────

    @Transactional
    public ForgotPasswordResponse sendOtp(String email) {
        // Vérifier si l'email existe
        if (!userRepository.existsByEmail(email)) {
            // Réponse volontairement vague pour ne pas révéler si l'email existe
            return new ForgotPasswordResponse(
                    "Si cette adresse existe, un code de vérification a été envoyé.", true
            );
        }

        // Générer un code OTP à 6 chiffres
        String otpCode = generateOtp();

        // Créer et sauvegarder l'entité OTP
        PasswordResetOtp otp = PasswordResetOtp.builder()
                .email(email)
                .otpCode(otpCode)
                .expiresAt(LocalDateTime.now().plusMinutes(OTP_VALIDITY_MINUTES))
                .build();

        otpRepository.save(otp);

        // Envoyer l'OTP par e-mail
        emailService.sendOtpEmail(email, otpCode);

        log.info("OTP de réinitialisation généré pour {}", email);

        return new ForgotPasswordResponse(
                "Un code de vérification a été envoyé à votre adresse e-mail.", true
        );
    }

    // ────────────────────────────────────────────────────────────────
    // Étape 2 : Vérifier le code OTP et renvoyer un token de reset
    // ────────────────────────────────────────────────────────────────

    @Transactional
    public ForgotPasswordResponse verifyOtp(String email, String otpCode) {
        // Récupérer le dernier OTP non vérifié pour cet email
        PasswordResetOtp otp = otpRepository
                .findTopByEmailAndVerifiedFalseOrderByCreatedAtDesc(email)
                .orElse(null);

        if (otp == null) {
            return new ForgotPasswordResponse(
                    "Aucune demande de réinitialisation trouvée. Veuillez recommencer.", false
            );
        }

        // Vérifier si le nombre max de tentatives est atteint
        if (otp.hasExceededMaxAttempts()) {
            return new ForgotPasswordResponse(
                    "Nombre maximum de tentatives atteint. Veuillez recommencer la procédure.", false
            );
        }

        // Vérifier si l'OTP a expiré
        if (otp.isExpired()) {
            return new ForgotPasswordResponse(
                    "Le code de vérification a expiré. Veuillez en demander un nouveau.", false
            );
        }

        // Vérifier le code
        if (!otp.getOtpCode().equals(otpCode)) {
            otp.setAttempts(otp.getAttempts() + 1);
            otpRepository.save(otp);

            int remaining = MAX_ATTEMPTS - otp.getAttempts();

            if (remaining <= 0) {
                return new ForgotPasswordResponse(
                        "Nombre maximum de tentatives atteint. Veuillez recommencer la procédure.", false
                );
            }

            return new ForgotPasswordResponse(
                    "Code incorrect. Il vous reste " + remaining + " tentative(s).", false
            );
        }

        // OTP valide → marquer comme vérifié
        otp.setVerified(true);
        otpRepository.save(otp);

        // Générer un token de réinitialisation valide 10 minutes
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        String resetToken = buildResetToken(user);

        log.info("OTP vérifié avec succès pour {}", email);

        return new ForgotPasswordResponse(
                "Code vérifié avec succès. Vous pouvez maintenant réinitialiser votre mot de passe.", true, resetToken
        );
    }

    // ────────────────────────────────────────────────────────────────
    // Étape 3 : Réinitialiser le mot de passe avec le token
    // ────────────────────────────────────────────────────────────────

    @Transactional
    public ForgotPasswordResponse resetPassword(String resetToken, String newPassword) {
        // Valider le token
        String email;
        try {
            email = jwtService.extractUsername(resetToken);

            // Vérifier le claim "purpose" pour s'assurer que c'est un token de reset
            String purpose = jwtService.extractClaim(resetToken, claims -> claims.get("purpose", String.class));
            if (!"password_reset".equals(purpose)) {
                return new ForgotPasswordResponse("Token invalide.", false);
            }
        } catch (Exception e) {
            return new ForgotPasswordResponse(
                    "Le lien de réinitialisation est invalide ou a expiré.", false
            );
        }

        // Vérifier que le token n'est pas expiré
        try {
            if (jwtService.extractClaim(resetToken,
                    claims -> claims.getExpiration().before(new java.util.Date()))) {
                return new ForgotPasswordResponse(
                        "Le lien de réinitialisation a expiré. Veuillez recommencer.", false
                );
            }
        } catch (Exception e) {
            return new ForgotPasswordResponse(
                    "Le lien de réinitialisation a expiré. Veuillez recommencer.", false
            );
        }

        // Récupérer l'utilisateur et mettre à jour le mot de passe
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Nettoyer les OTP utilisés
        otpRepository.deleteAllByEmail(email);

        log.info("Mot de passe réinitialisé avec succès pour {}", email);

        return new ForgotPasswordResponse(
                "Votre mot de passe a été réinitialisé avec succès.", true
        );
    }

    // ────────────────────────────────────────────────────────────────
    // Méthodes utilitaires
    // ────────────────────────────────────────────────────────────────

    /** Génère un OTP numérique à 6 chiffres */
    private String generateOtp() {
        SecureRandom random = new SecureRandom();
        int otp = 100_000 + random.nextInt(900_000); // 100000 à 999999
        return String.valueOf(otp);
    }

    /** Construit un JWT de réinitialisation avec une durée de vie de 10 minutes */
    private String buildResetToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("purpose", "password_reset");
        return jwtService.generateTokenWithExpiration(claims, user, RESET_TOKEN_EXPIRATION_MS);
    }
}
