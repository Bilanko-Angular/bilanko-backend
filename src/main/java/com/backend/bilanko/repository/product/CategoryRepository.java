package com.backend.bilanko.repository.product;

import com.backend.bilanko.models.object.product.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

@Service
public interface CategoryRepository extends JpaRepository<Category,Long> {

}
