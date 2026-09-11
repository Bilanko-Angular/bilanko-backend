package com.backend.bilanko.DTO.concept.transaction;

public record TopSoldProductDTO(
        long productId,
        String productName,
        String reference,
        long quantitySold,
        double revenue,
        double margin
) {
}
