package com.backend.bilanko.DTO.concept.transaction;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record SaleItemRequestDTO(
        @NotNull
        Long productId,
        @Min(1)
        int quantity,
        Double unitSellingPrice
) {
}
