package com.backend.bilanko.DTO.concept.transaction;

import com.backend.bilanko.DTO.object.product.StockOverviewDTO;

import java.time.LocalDateTime;

public record OverviewSummaryDTO(
        double revenue,
        double grossMargin,
        double totalCharges,
        double netProfit,
        long salesCount,
        long chargesCount,
        StockOverviewDTO stock,
        LocalDateTime from,
        LocalDateTime to
) {
}
