package com.backend.bilanko.repository.object.product;

import com.backend.bilanko.models.object.product.Product;
import com.backend.bilanko.models.person.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    // Tous les produits appartenant à un user (via son email)
    List<Product> findByUserEmail(String email);

    int countByUser_Id(long userId);

    long countByCreatedAtGreaterThanEqual(Instant start);

    long countByCreatedAtBetween(Instant from, Instant to);

    @Query("""
            SELECT p FROM Product p
            WHERE p.quantity = 0
               OR (p.alertThreshold IS NOT NULL AND p.quantity <= p.alertThreshold)
            ORDER BY p.quantity ASC, p.name ASC
            """)
    List<Product> findStockAlerts(Pageable pageable);

    @Query("SELECT COUNT(DISTINCT si.product.id) FROM SaleItem si")
    long countAssociatedWithSale();

    @Query("SELECT AVG(p.price) FROM Product p")
    Double averageProductPrice();

    @Query("""
            SELECT DISTINCT p FROM Product p
            LEFT JOIN p.categories c
            WHERE (
                LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(p.reference) LIKE LOWER(CONCAT('%', :keyword, '%'))
            )
            AND (:categoryId IS NULL OR c.id = :categoryId)
            AND (:minPurchasePrice IS NULL OR p.purchasePrice >= :minPurchasePrice)
            AND (:maxPurchasePrice IS NULL OR p.purchasePrice <= :maxPurchasePrice)
            AND (:minPrice IS NULL OR p.price >= :minPrice)
            AND (:maxPrice IS NULL OR p.price <= :maxPrice)
            """)
    Page<Product> adminSearchProducts(
            @Param("keyword") String keyword,
            @Param("categoryId") Long categoryId,
            @Param("minPurchasePrice") Double minPurchasePrice,
            @Param("maxPurchasePrice") Double maxPurchasePrice,
            @Param("minPrice") Double minPrice,
            @Param("maxPrice") Double maxPrice,
            Pageable pageable);

    Product findFirstByReference(String ref);

    // ---------------------------------------------------------------
    // Recherche globale : filtre par keyword directement en BDD
    // + JOIN FETCH des catégories pour éviter le N+1
    // ---------------------------------------------------------------
    @Query("""
            SELECT DISTINCT p FROM Product p
            LEFT JOIN FETCH p.categories c
            WHERE p.user.email = :email
            AND (
                LOWER(p.name)      LIKE LOWER(CONCAT('%', :kw, '%'))
             OR LOWER(p.reference) LIKE LOWER(CONCAT('%', :kw, '%'))
             OR EXISTS (
                    SELECT 1 FROM p.categories cat
                    WHERE LOWER(cat.name) LIKE LOWER(CONCAT('%', :kw, '%'))
                )
            )
            """)
    List<Product> searchByUserEmailAndKeyword(
            @Param("email") String email,
            @Param("kw")    String keyword);

    int countByUser_Id(Long userId);
    long countByUserAndCreatedAtBetween(User user, Instant start, Instant end);

    @Query("""
        SELECT c.name 
        FROM Product p 
        JOIN p.categories c 
        WHERE p.user = :user 
        GROUP BY c.id, c.name 
        ORDER BY COUNT(p) DESC
        """)
    List<String> findMostUsedCategoryName(@Param("user") User user, Pageable pageable);

    @Query("SELECT COALESCE(AVG(p.price), 0.0) FROM Product p WHERE p.user = :user")
    Double findAveragePriceByUser(@Param("user") User user);
}
