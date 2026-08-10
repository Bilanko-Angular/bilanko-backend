package com.backend.bilanko.services.object.product;

import com.backend.bilanko.DTO.object.product.CategoryDTO;
import com.backend.bilanko.models.object.product.Category;

public interface CategoryServices {
    public Category create(CategoryDTO categoryDTO,String email);
}
