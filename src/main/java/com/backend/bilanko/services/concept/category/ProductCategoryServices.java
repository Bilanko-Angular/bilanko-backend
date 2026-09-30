package com.backend.bilanko.services.concept.category;


import com.backend.bilanko.DTO.concept.category.CleanCategoryDTO;
import com.backend.bilanko.models.concept.category.BaseCategoryModel;
import com.backend.bilanko.models.concept.category.CategoryType;
import com.backend.bilanko.models.concept.category.ProductCategory;
import com.backend.bilanko.repository.concept.category.ProductCategoryRepository;
import com.backend.bilanko.utils.annotation.AdminOnly;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductCategoryServices implements CategoryService{
    private final ProductCategoryRepository productCategoryRepository;
    @Override
    public CategoryType type() {
        return CategoryType.PRODUCT;
    }


    @Override
    @AdminOnly
    public BaseCategoryModel create(String name) {
        ProductCategory productCategory = new ProductCategory();
        productCategory.setName(name);
        return productCategoryRepository.save(productCategory);
    }

    @Override
    public List<ProductCategory> findAll() {
        return productCategoryRepository.findAll();
    }

    @Override
    @AdminOnly
    public BaseCategoryModel findById(long id) {
        return productCategoryRepository.findById(id).orElse(null);
    }

    @Override
    @AdminOnly
    public BaseCategoryModel update(long id, String name) {
        ProductCategory category = productCategoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Catégorie introuvable : " + id));

        category.setName(name);
        return productCategoryRepository.save(category);
    }

    @Override
    @AdminOnly
    public void delete(long id) {
        productCategoryRepository.deleteById(id);
    }

    public List<CleanCategoryDTO> searchByName(String name) {
        return productCategoryRepository.findByNameContainingIgnoreCase(name)
                .stream()
                .map(cat -> CleanCategoryDTO.builder()
                        .id(cat.getId())
                        .name(cat.getName())
                        .categoryType(CategoryType.PRODUCT)
                        .createAt(cat.getCreatedAt())
                        .build())
                .toList();
    }

    public List<CleanCategoryDTO> getCategoriesByNames(List<String> names) {
        return productCategoryRepository.findByNameIn(names)
                .stream()
                .map(cat -> CleanCategoryDTO.builder()
                        .id(cat.getId())
                        .name(cat.getName())
                        .categoryType(CategoryType.PRODUCT)
                        .createAt(cat.getCreatedAt())
                        .build())
                .toList();
    }

}
