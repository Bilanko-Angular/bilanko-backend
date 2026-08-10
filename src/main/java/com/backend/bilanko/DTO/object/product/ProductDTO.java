package com.backend.bilanko.DTO.object.product;

import jakarta.validation.constraints.PositiveOrZero;

import java.util.List;
import java.util.Optional;

public record ProductDTO(
        long id,
        String name,
        @PositiveOrZero
        int quantity,
        @PositiveOrZero
        double price,
        Optional<List<Long>> idCategories
) {}
