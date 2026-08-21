package com.backend.bilanko.DTO.concept.transaction;

import java.time.LocalDate;

public record ChargeResponseDTO(
        long id,
        String label,
        String supplier,
        double amount,
        LocalDate date
) {
}
