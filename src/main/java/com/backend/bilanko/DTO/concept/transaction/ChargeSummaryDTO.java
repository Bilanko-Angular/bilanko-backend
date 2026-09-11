package com.backend.bilanko.DTO.concept.transaction;

import java.time.LocalDate;

public record ChargeSummaryDTO(
        long chargesCount,
        double totalAmount,
        LocalDate from,
        LocalDate to
) {
}
