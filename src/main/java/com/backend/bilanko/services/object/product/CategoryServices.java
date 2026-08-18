package com.backend.bilanko.services.object.product;

import com.backend.bilanko.DTO.object.product.CategoryDTO;
import com.backend.bilanko.models.object.product.Category;

import java.util.List;

public interface CategoryServices {
    Category create(CategoryDTO categoryDTO, String email);
    List<Category> findAll();
    Category findById(long id);
    Category update(long id, CategoryDTO categoryDTO, String email);
    void delete(long id, String email);
}
