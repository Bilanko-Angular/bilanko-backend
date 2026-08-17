package com.backend.bilanko.repository.concept.transaction;

import com.backend.bilanko.controller.concept.transaction.Sale;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SaleRepository extends JpaRepository<Sale, Long> {
    List<Sale> findByUserId(long userId);
}
