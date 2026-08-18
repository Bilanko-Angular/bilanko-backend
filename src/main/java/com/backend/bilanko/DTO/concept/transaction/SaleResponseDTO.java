package com.backend.bilanko.DTO.concept.transaction;

import java.time.LocalDateTime;
import java.util.List;

public record SaleResponseDTO(
        long id,
        LocalDateTime saleDate,
        String customerName,
        double totalAmount,
        double totalMargin,
        List<SaleItemResponseDTO> items
) {
}
