package com.backend.bilanko.services.concept.category;


import com.backend.bilanko.models.concept.category.BaseCategoryModel;
import com.backend.bilanko.models.concept.category.CategoryType;
import com.backend.bilanko.utils.annotation.AdminOnly;
import com.backend.bilanko.utils.annotation.MerchantOnly;

import java.util.List;

public interface CategoryService {
    CategoryType type();

    @AdminOnly
    BaseCategoryModel create(String name);

    List<? extends BaseCategoryModel> findAll();

    @AdminOnly
    BaseCategoryModel findById(long id);

    @AdminOnly
    BaseCategoryModel update(long id, String name);

    @AdminOnly
    void delete(long id);
}
