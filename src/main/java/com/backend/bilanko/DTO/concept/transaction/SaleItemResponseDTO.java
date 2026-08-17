package com.backend.bilanko.DTO.concept.transaction;

public record SaleItemResponseDTO(
        long id,
        long productId,
        String productName,
        int quantity,
        double unitSellingPrice,
        double unitPurchasePrice,
        double margin
) {
}
