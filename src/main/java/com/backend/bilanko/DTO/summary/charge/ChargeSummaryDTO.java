package com.backend.bilanko.DTO.summary.charge;

import java.time.LocalDate;

public record ChargeSummaryDTO(
        long chargesCount,
        double totalAmount,
        LocalDate from,
        LocalDate to
) {
}
