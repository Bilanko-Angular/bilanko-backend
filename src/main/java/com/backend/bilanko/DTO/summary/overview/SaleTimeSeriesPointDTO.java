package com.backend.bilanko.DTO.summary.overview;

public record SaleTimeSeriesPointDTO(
        String period,
        long salesCount,
        double totalAmount,
        double totalMargin
) {
}
