package com.backend.bilanko.repository.transaction;

import com.backend.bilanko.models.transaction.ChargeCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChargeCategoryRepository extends JpaRepository<ChargeCategory, Long> {
}
