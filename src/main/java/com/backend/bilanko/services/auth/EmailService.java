package com.backend.bilanko.services.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    /**
     * Envoie un e-mail contenant le code OTP pour la réinitialisation de mot de passe.
     *
     * @param to      adresse e-mail du destinataire
     * @param otpCode code OTP à 6 chiffres
     */
    public void sendOtpEmail(String to, String otpCode) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject("Bilanko - Code de réinitialisation de mot de passe");
            message.setText(
                    "Bonjour,\n\n" +
                    "Vous avez demandé la réinitialisation de votre mot de passe sur Bilanko.\n\n" +
                    "Votre code de vérification est : " + otpCode + "\n\n" +
                    "Ce code est valide pendant 5 minutes.\n\n" +
                    "Si vous n'avez pas fait cette demande, veuillez ignorer cet e-mail.\n\n" +
                    "L'équipe Bilanko"
            );
            mailSender.send(message);
            log.info("OTP envoyé avec succès à {}", to);
        } catch (Exception e) {
            log.error("Erreur lors de l'envoi de l'e-mail OTP à {}: {}", to, e.getMessage());
            throw new RuntimeException("Impossible d'envoyer l'e-mail de vérification");
        }
    }
}
