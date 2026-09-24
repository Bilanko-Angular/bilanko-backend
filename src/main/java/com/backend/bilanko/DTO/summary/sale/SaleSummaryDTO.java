package com.backend.bilanko.DTO.summary.sale;

import java.time.LocalDateTime;

public record SaleSummaryDTO(
        long salesCount,
        double totalAmount,
        double totalMargin,
        LocalDateTime from,
        LocalDateTime to
) {
}
