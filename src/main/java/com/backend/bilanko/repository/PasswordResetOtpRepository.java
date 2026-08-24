package com.backend.bilanko.repository;

import com.backend.bilanko.models.person.PasswordResetOtp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PasswordResetOtpRepository extends JpaRepository<PasswordResetOtp, Long> {

    /**
     * Récupère le dernier OTP non vérifié pour un email donné,
     * trié par date de création décroissante.
     */
    Optional<PasswordResetOtp> findTopByEmailAndVerifiedFalseOrderByCreatedAtDesc(String email);

    /** Supprime tous les OTP associés à un email (nettoyage après reset réussi) */
    void deleteAllByEmail(String email);
}
