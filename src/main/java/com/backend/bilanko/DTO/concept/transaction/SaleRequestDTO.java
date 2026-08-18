package com.backend.bilanko.DTO.concept.transaction;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.time.LocalDateTime;
import java.util.List;

public record SaleRequestDTO(
        LocalDateTime saleDate,
        String customerName,
        @NotEmpty
        @Valid List<SaleItemRequestDTO> items
) {
}
