package com.backend.bilanko.DTO.object.product;

import java.util.List;

public record CategoryDTO(
        String name,
        List<Long> idProducts
) {
    public CategoryDTO {
        // Vérification explicite du null
        idProducts = (idProducts == null) ? List.of() : idProducts;
    }
}
