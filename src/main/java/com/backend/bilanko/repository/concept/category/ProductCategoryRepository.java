package com.backend.bilanko.repository.concept.category;

import com.backend.bilanko.models.concept.category.ProductCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;


public interface ProductCategoryRepository extends JpaRepository<ProductCategory,Long> {
    List<ProductCategory> findByNameContainingIgnoreCase(String name);
    List<ProductCategory> findByNameIn(List<String> names);

    @Query(value = """
        SELECT count(DISTINCT u.id) AS total
        FROM product_categories pcp
            INNER JOIN product p ON p.id=pcp.product_id
                INNER JOIN users u ON p.user_id=u.id
    """,nativeQuery = true)
    Long totalUser();
}
