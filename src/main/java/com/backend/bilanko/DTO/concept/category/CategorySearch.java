package com.backend.bilanko.DTO.concept.category;

import com.backend.bilanko.models.concept.category.CategoryType;

public record CategorySearch(
        Long id,
        String name,
        Integer numElement,
        Integer numUser,
        CategoryType categoryType
) {}
