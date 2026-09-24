package com.backend.bilanko.DTO.summary.overview;

public record TopSoldProductDTO(
        long productId,
        String productName,
        String reference,
        long quantitySold,
        double revenue,
        double margin
) {
}
