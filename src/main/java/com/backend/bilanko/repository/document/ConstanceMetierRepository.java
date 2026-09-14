package com.backend.bilanko.repository.document;

import com.backend.bilanko.models.document.ConstanceMetier;
import com.backend.bilanko.models.document.TypeConstanceMetier;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ConstanceMetierRepository extends JpaRepository<ConstanceMetier, Long> {
    Optional<ConstanceMetier> findByType(TypeConstanceMetier type);

    boolean existsByType(TypeConstanceMetier type);
}
