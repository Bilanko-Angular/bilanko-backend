package com.backend.bilanko.DTO.object.product;

import java.time.Instant;
import java.util.List;

public record ProductApiDTO(
        long id,
        String name,
        int quantity,
        double price,
        double purchasePrice,
        List<CategoryDTO> categories,
        String reference,
        Integer alertThreshold,
        Instant createdAt
) {
    public record CategoryDTO(
            long id,
            String name
    ) {
    }
}
