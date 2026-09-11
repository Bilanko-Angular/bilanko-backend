package com.backend.bilanko.DTO.object.product;

public record StockOverviewDTO(
        long totalProducts,
        long outOfStock,
        long lowStock,
        double stockValueAtPurchase,
        double stockValueAtSale
) {
}
