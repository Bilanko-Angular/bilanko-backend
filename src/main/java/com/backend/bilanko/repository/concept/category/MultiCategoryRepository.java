package com.backend.bilanko.repository.concept.category;

import com.backend.bilanko.models.concept.category.ProductCategory;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

public interface MultiCategoryRepository extends Repository<ProductCategory, Long> {

    @Query(value = """
           SELECT id, name, 'PRODUCT' AS type
           FROM product_category
           WHERE lower(unaccent(name)) LIKE lower(unaccent(concat('%', :keyword, '%')))
           UNION ALL
           SELECT id, name, 'SERVICE' AS type
           FROM service_category
           WHERE lower(unaccent(name)) LIKE lower(unaccent(concat('%', :keyword, '%')))
           """, nativeQuery = true)
    List<CategorySearchView> searchAll(@Param("keyword") String keyword);
}
