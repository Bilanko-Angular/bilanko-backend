package com.backend.bilanko.DTO.transaction.sale;

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
