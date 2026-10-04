package com.backend.bilanko.DTO.concept.category;

import com.backend.bilanko.models.concept.category.CategoryType;
import lombok.Builder;

import java.time.Instant;

@Builder
public record CleanCategoryDTO(
        Long id,
        String name,
        CategoryType categoryType,
        Instant createAt
) {
}
