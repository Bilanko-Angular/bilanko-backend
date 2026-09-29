package com.backend.bilanko.services.concept.category;

import com.backend.bilanko.models.concept.category.BaseCategoryModel;
import com.backend.bilanko.models.concept.category.CategoryType;
import com.backend.bilanko.models.concept.category.ChargeCategory;
import com.backend.bilanko.repository.concept.category.ChargeCategoryRepository;
import com.backend.bilanko.utils.annotation.AdminOnly;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChargeCategoryService implements CategoryService{
    private final ChargeCategoryRepository chargeCategoryRepository;
    @Override
    public CategoryType type() {
        return CategoryType.PRODUCT;
    }


    @Override
    @AdminOnly
    public BaseCategoryModel create(String name) {
        ChargeCategory chargeCategory = new ChargeCategory();
        chargeCategory.setName(name);
        return chargeCategoryRepository.save(chargeCategory);
    }

    @Override
    public List<ChargeCategory> findAll() {
        return chargeCategoryRepository.findAll();
    }

    @Override
    @AdminOnly
    public BaseCategoryModel findById(long id) {
        return chargeCategoryRepository.findById(id).orElse(null);
    }

    @Override
    @AdminOnly
    public BaseCategoryModel update(long id, String name) {
        ChargeCategory category = chargeCategoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Catégorie introuvable : " + id));

        category.setName(name);
        return chargeCategoryRepository.save(category);
    }

    @Override
    @AdminOnly
    public void delete(long id) {
        chargeCategoryRepository.deleteById(id);
    }
}
