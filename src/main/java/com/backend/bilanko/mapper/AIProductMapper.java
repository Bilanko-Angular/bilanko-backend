package com.backend.bilanko.mapper;

import com.backend.bilanko.DTO.ai.ProductDescriptionClean;
import com.backend.bilanko.DTO.ai.ProductRecognitionResponseDTO;
import com.backend.bilanko.DTO.object.product.CleanCategoryDTO;
import com.backend.bilanko.services.object.product.CategoryServices;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;
import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
public final class AIProductMapper {

    private final CategoryServices categoryServices;

    public ProductDescriptionClean cleanAiImageJsonReponse(ProductRecognitionResponseDTO dto) {
        if (dto == null) {
            return ProductDescriptionClean.builder().build();
        }

        return ProductDescriptionClean.builder()
                .name(defaultString(dto.suggestedName()))
                .quantity(0)
                .price(defaultDouble(dto.suggestedPrice()))
                .purchasePrice(0.0)
                .categoryDTOS(mapCategories(dto.matchedCategoryNames()))
                .suggestCategories(defaultList(dto.newCategorySuggestions()))
                .comments(defaultString(dto.rawNotes()))
                .build();
    }

    public ProductDescriptionClean cleanAiVoiceJsonReponse(ProductRecognitionResponseDTO dto) {
        if (dto == null) {
            return ProductDescriptionClean.builder().build();
        }

        return ProductDescriptionClean.builder()
                .name(defaultString(dto.suggestedName()))
                .quantity(defaultInt(dto.suggestedQuantity()))
                .price(defaultDouble(dto.suggestedPrice()))
                .purchasePrice(defaultDouble(dto.suggestedPurchasePrice()))
                .categoryDTOS(mapCategories(dto.matchedCategoryNames()))
                .suggestCategories(defaultList(dto.newCategorySuggestions()))
                .comments(defaultString(dto.rawNotes()))
                .build();
    }

    // --- Méthodes utilitaires de sécurisation contre les nulls ---

    private List<CleanCategoryDTO> mapCategories(List<String> categoryNames) {
        if (categoryNames == null || categoryNames.isEmpty()) {
            return Collections.emptyList();
        }
        List<CleanCategoryDTO> result = categoryServices.getCategoriesByNames(categoryNames);
        return result != null ? result : Collections.emptyList();
    }

    private String defaultString(String val) {
        return val != null ? val : "";
    }

    private int defaultInt(Integer val) {
        return (val != null && val >= 0) ? val : 0;
    }

    private double defaultDouble(Double val) {
        return (val != null && val >= 0.0) ? val : 0.0;
    }

    private <T> List<T> defaultList(List<T> list) {
        return list != null ? list : Collections.emptyList();
    }
}