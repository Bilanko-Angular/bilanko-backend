package com.backend.bilanko.DTO.summary.product;

import lombok.Builder;

@Builder
public record ProductUserSummary(
        int total,
        Long onTheMount,
        String mainCategory,
        Double averagePrice
) {
}
