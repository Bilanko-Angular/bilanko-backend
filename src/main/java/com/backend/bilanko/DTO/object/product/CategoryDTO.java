package com.backend.bilanko.DTO.object.product;

import java.util.List;
import java.util.Optional;

public record CategoryDTO(
        String name,
        Optional<List<Long>> idProducts
) {
}
