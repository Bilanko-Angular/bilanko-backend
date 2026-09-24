package com.backend.bilanko.repository.object.product;

import com.backend.bilanko.models.object.product.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface CategoryRepository extends JpaRepository<Category,Long> {
    List<Category> findByNameIn(List<String> names);
    List<Category> findByNameContainingIgnoreCase(String name);
}
