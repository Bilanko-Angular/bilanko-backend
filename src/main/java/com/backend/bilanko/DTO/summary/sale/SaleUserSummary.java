package com.backend.bilanko.DTO.summary.sale;

import lombok.Builder;

@Builder
public record SaleUserSummary(
        Long totalSales,
        Long monthlySales,
        double totalMargin,
        String bestCustomer
) {
}
