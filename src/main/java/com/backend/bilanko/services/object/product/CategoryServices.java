package com.backend.bilanko.services.object.product;

import com.backend.bilanko.DTO.object.product.CategoryDTO;
import com.backend.bilanko.DTO.object.product.CleanCategoryDTO;
import com.backend.bilanko.models.concept.category.ProductCategory;

import java.util.List;

public interface CategoryServices {
    ProductCategory create(CategoryDTO categoryDTO, String email);
    List<ProductCategory> findAll();
    ProductCategory findById(long id);
    List<CleanCategoryDTO> searchByName(String name);
    public List<CleanCategoryDTO> getCategoriesByNames(List<String> names);
    ProductCategory update(long id, CategoryDTO categoryDTO, String email);
    void delete(long id, String email);
}
