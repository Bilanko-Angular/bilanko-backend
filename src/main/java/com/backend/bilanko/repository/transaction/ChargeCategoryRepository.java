package com.backend.bilanko.repository.transaction;

import com.backend.bilanko.models.concept.category.ChargeCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChargeCategoryRepository extends JpaRepository<ChargeCategory, Long> {
}
