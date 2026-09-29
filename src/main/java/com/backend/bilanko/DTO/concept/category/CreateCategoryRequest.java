package com.backend.bilanko.DTO.concept.category;

import com.backend.bilanko.models.concept.category.CategoryType;

public record CreateCategoryRequest(
        String name,
        CategoryType categoryType
) {
}
