package com.backend.bilanko.services.concept.category;

import com.backend.bilanko.DTO.concept.category.CategorySearch;
import com.backend.bilanko.models.concept.category.CategoryType;
import com.backend.bilanko.repository.concept.category.MultiCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MultiCategoryService {
    private final MultiCategoryRepository multiCategoryRepository;

    public List<CategorySearch> search(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return List.of();
        }
        return multiCategoryRepository.searchAll(keyword.trim()).stream()
                .map(v -> new CategorySearch(
                        v.getId(),
                        v.getName(),
                        v.getUsageCount().intValue(),
                        v.getUserCount().intValue(),
                        CategoryType.valueOf(v.getType())))
                .toList();
    }
}
