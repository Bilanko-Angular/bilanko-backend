package com.backend.bilanko.DTO.transaction.charge;

import java.time.LocalDate;

public record ChargeResponseDTO(
        long id,
        String label,
        String supplier,
        double amount,
        LocalDate date,
        Long categoryId,
        String categoryName
) {
}
