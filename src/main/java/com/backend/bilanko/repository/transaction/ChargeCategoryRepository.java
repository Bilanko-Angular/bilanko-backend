package com.backend.bilanko.repository.transaction;

import com.backend.bilanko.models.transaction.ChargeCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChargeCategoryRepository extends JpaRepository<ChargeCategory, Long> {
    Optional<ChargeCategory> findByName(String name);
}
