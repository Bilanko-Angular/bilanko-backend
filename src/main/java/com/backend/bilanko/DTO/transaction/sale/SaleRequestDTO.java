package com.backend.bilanko.DTO.transaction.sale;

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
