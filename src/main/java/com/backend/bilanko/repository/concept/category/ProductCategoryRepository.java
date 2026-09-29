package com.backend.bilanko.repository.concept.category;

import com.backend.bilanko.models.concept.category.ProductCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface ProductCategoryRepository extends JpaRepository<ProductCategory,Long> {

}
