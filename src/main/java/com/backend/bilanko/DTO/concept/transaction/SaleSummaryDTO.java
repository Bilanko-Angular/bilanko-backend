package com.backend.bilanko.DTO.concept.transaction;

import java.time.LocalDateTime;

public record SaleSummaryDTO(
        long salesCount,
        double totalAmount,
        double totalMargin,
        LocalDateTime from,
        LocalDateTime to
) {
}
