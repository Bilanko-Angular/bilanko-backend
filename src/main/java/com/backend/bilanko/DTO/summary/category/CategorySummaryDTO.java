package com.backend.bilanko.DTO.summary.category;

import lombok.Builder;

@Builder
public record CategorySummaryDTO (
        Long totalCategory,
        Long totalProduct,
        Long totalCharge,
        Long actifUser
){}
