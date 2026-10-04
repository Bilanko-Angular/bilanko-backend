package com.backend.bilanko.DTO.concept.category;

import com.backend.bilanko.models.concept.category.CategoryType;

import java.util.List;

public record CategoryDTO(
        Long id,
        String name,
        CategoryType categoryType,
        List<Long> idElements
) {
    public CategoryDTO {
        // Vérification explicite du null
        idElements = (idElements == null) ? List.of() : idElements;
    }
}
