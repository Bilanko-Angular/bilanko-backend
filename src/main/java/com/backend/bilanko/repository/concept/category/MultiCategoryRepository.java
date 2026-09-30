package com.backend.bilanko.repository.concept.category;

import com.backend.bilanko.DTO.concept.category.CategorySearchView;
import com.backend.bilanko.models.concept.category.ProductCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;


public interface MultiCategoryRepository extends Repository<ProductCategory, Long> {

    @Query(value = """
       SELECT c.id, c.name, 'PRODUCT' AS type,
              (SELECT count(DISTINCT pcp.product_id)
               FROM product_categories pcp
               WHERE pcp.categories_id = c.id) AS "usageCount",
              (SELECT count(DISTINCT p.user_id)
               FROM product_categories pcp
               JOIN product p ON p.id = pcp.product_id
               WHERE pcp.categories_id = c.id) AS "userCount"
       FROM category c
       WHERE (CAST(:type AS text) IS NULL OR CAST(:type AS text) = 'PRODUCT')
         AND lower(unaccent(c.name)) LIKE lower(unaccent(concat('%', :keyword, '%')))

       UNION ALL

       SELECT c.id, c.name, 'CHARGE' AS type,
              (SELECT count(*) FROM charge ch
               WHERE ch.charge_category_id = c.id) AS "usageCount",
              (SELECT count(DISTINCT ch.user_id) FROM charge ch
               WHERE ch.charge_category_id = c.id) AS "userCount"
       FROM charge_category c
       WHERE (CAST(:type AS text) IS NULL OR CAST(:type AS text) = 'CHARGE')
         AND lower(unaccent(c.name)) LIKE lower(unaccent(concat('%', :keyword, '%')))

       ORDER BY name, type, id
       """,
            countQuery = """
       SELECT
         (SELECT count(*) FROM category c
          WHERE (CAST(:type AS text) IS NULL OR CAST(:type AS text) = 'PRODUCT')
            AND lower(unaccent(c.name)) LIKE lower(unaccent(concat('%', :keyword, '%'))))
       + (SELECT count(*) FROM charge_category c
          WHERE (CAST(:type AS text) IS NULL OR CAST(:type AS text) = 'CHARGE')
            AND lower(unaccent(c.name)) LIKE lower(unaccent(concat('%', :keyword, '%'))))
       """,
            nativeQuery = true)
    Page<CategorySearchView> searchAll(@Param("keyword") String keyword,
                                       @Param("type") String type,
                                       Pageable pageable);
}
