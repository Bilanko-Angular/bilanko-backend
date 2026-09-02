package com.backend.bilanko.DTO.ai;

import com.backend.bilanko.DTO.object.product.CleanCategoryDTO;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Builder;

import java.util.List;

@Builder
public record ProductDescriptionClean(
        String name,
        @PositiveOrZero
        int quantity,
        @PositiveOrZero
        double price,
        List<CleanCategoryDTO> categoryDTOS,
        List<String> suggestCategories,
        String comments
) {}
