package com.backend.bilanko.repository.object.product;

import com.backend.bilanko.models.concept.category.ProductCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface CategoryRepository extends JpaRepository<ProductCategory,Long> {
    List<ProductCategory> findByNameIn(List<String> names);
    List<ProductCategory> findByNameContainingIgnoreCase(String name);
}
