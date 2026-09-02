package com.backend.bilanko.services.object.product;

import com.backend.bilanko.DTO.object.product.CategoryDTO;
import com.backend.bilanko.DTO.object.product.CleanCategoryDTO;
import com.backend.bilanko.models.object.product.Category;

import java.util.List;

public interface CategoryServices {
    Category create(CategoryDTO categoryDTO, String email);
    List<Category> findAll();
    Category findById(long id);
    List<Category> searchByName(String name);
    public List<CleanCategoryDTO> getCategoriesByNames(List<String> names);
    Category update(long id, CategoryDTO categoryDTO, String email);
    void delete(long id, String email);
}
