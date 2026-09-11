package com.backend.bilanko.DTO.concept.transaction;

public record SaleTimeSeriesPointDTO(
        String period,
        long salesCount,
        double totalAmount,
        double totalMargin
) {
}
