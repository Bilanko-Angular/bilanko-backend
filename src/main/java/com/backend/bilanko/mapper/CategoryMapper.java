package com.backend.bilanko.mapper;

import com.backend.bilanko.DTO.concept.category.CleanCategoryDTO;
import com.backend.bilanko.models.concept.category.BaseCategoryModel;
import com.backend.bilanko.models.concept.category.CategoryType;

public final class CategoryMapper {
    private CategoryMapper() {}
    public  static CleanCategoryDTO fromBaseCategoryToCleanCategoryDTO(BaseCategoryModel baseCategoryModel, CategoryType categoryType) {
        return CleanCategoryDTO.builder()
                .id(baseCategoryModel.getId())
                .name(baseCategoryModel.getName())
                .categoryType(categoryType)
                .createAt(baseCategoryModel.getCreatedAt())
                .build();
    }
}
