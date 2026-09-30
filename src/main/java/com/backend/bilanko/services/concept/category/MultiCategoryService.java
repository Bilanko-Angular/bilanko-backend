package com.backend.bilanko.services.concept.category;

import com.backend.bilanko.DTO.concept.category.CategorySearch;
import com.backend.bilanko.DTO.shared.PageResponse;
import com.backend.bilanko.DTO.summary.category.CategorySummaryDTO;
import com.backend.bilanko.models.concept.category.CategoryType;
import com.backend.bilanko.repository.concept.category.ChargeCategoryRepository;
import com.backend.bilanko.repository.concept.category.MultiCategoryRepository;
import com.backend.bilanko.repository.concept.category.ProductCategoryRepository;
import com.backend.bilanko.utils.annotation.AdminOnly;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MultiCategoryService {
    private final MultiCategoryRepository multiCategoryRepository;
    private final ProductCategoryRepository pcpRepository;
    private final ChargeCategoryRepository cpRepository;

    // Avec filtre : le mot-clé est obligatoire
    @AdminOnly
    public PageResponse<CategorySearch> search(String keyword, CategoryType type,
                                               int page, int size) {
        if (keyword == null || keyword.isBlank()) {
            return PageResponse.from(Page.empty(pageable(page, size)));
        }
        return query(keyword.trim(), type, page, size);
    }

    // Sans filtre : tout, avec un type facultatif
    @AdminOnly
    public PageResponse<CategorySearch> findAll(CategoryType type, int page, int size) {
        return query("", type, page, size);
    }

    @AdminOnly
    public CategorySummaryDTO summary(){

        return CategorySummaryDTO.builder()
                .totalCategory(cpRepository.count()+pcpRepository.count())
                .totalCharge(cpRepository.count())
                .totalProduct(pcpRepository.count())
                .actifUser(pcpRepository.totalUser())
                .build();
    }
    private PageResponse<CategorySearch> query(String keyword, CategoryType type,
                                               int page, int size) {
        String typeName = type == null ? null : type.name();
        Page<CategorySearch> result = multiCategoryRepository
                .searchAll(keyword, typeName, pageable(page, size))
                .map(v -> new CategorySearch(
                        v.getId(),
                        v.getName(),
                        v.getUsageCount().intValue(),
                        v.getUserCount().intValue(),
                        CategoryType.valueOf(v.getType())));
        return PageResponse.from(result);
    }

    private Pageable pageable(int page, int size) {
        return PageRequest.of(Math.max(page, 0), Math.clamp(size, 1, 100));
    }


}
