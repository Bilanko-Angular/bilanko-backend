package com.backend.bilanko.repository.document;

import com.backend.bilanko.models.document.InfoCle;
import com.backend.bilanko.models.document.TypeInfoCle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InfoCleRepository extends JpaRepository<InfoCle, Long> {
    List<InfoCle> findByTypeOrderByNomAsc(TypeInfoCle type);

    Optional<InfoCle> findBySlug(String slug);

    boolean existsBySlug(String slug);
}
