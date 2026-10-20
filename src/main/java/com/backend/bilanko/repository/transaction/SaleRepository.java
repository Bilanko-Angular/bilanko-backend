package com.backend.bilanko.repository.transaction;

import com.backend.bilanko.DTO.summary.overview.TopSoldProductDTO;
import com.backend.bilanko.models.person.user.User;
import com.backend.bilanko.models.transaction.Sale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SaleRepository extends JpaRepository<Sale, Long> {
    List<Sale> findByUserOrderBySaleDateDesc(User user);

    Page<Sale> findByUserOrderBySaleDateDesc(User user, Pageable pageable);

    List<Sale> findByUserAndSaleDateBetweenOrderBySaleDateDesc(
            User user,
            LocalDateTime from,
            LocalDateTime to
    );

    Page<Sale> findByUserAndSaleDateBetweenOrderBySaleDateDesc(
            User user,
            LocalDateTime from,
            LocalDateTime to,
            Pageable pageable
    );

    Optional<Sale> findByIdAndUser(long id, User user);

    long countBySaleDateBetween(LocalDateTime from, LocalDateTime to);

    List<Sale> findBySaleDateBetween(LocalDateTime from, LocalDateTime to);

    List<Sale> findAllByOrderBySaleDateDesc(Pageable pageable);

    @Query("""
            SELECT DISTINCT s FROM Sale s
            LEFT JOIN s.items i
            LEFT JOIN i.product p
            WHERE s.user = :user
            AND (
                LOWER(s.customerName) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(p.reference) LIKE LOWER(CONCAT('%', :keyword, '%'))
            )
            ORDER BY s.saleDate DESC
            """)
    List<Sale> searchByKeyword(@Param("user") User user, @Param("keyword") String keyword);

    @Query("""
            SELECT DISTINCT s FROM Sale s
            LEFT JOIN s.items i
            LEFT JOIN i.product p
            WHERE s.user = :user
            AND (
                LOWER(s.customerName) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(p.reference) LIKE LOWER(CONCAT('%', :keyword, '%'))
            )
            ORDER BY s.saleDate DESC
            """)
    Page<Sale> searchByKeywordPaged(@Param("user") User user, @Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT SUM(s.totalAmount) FROM Sale s")
    Double sumAllTotalAmount();

    @Query("SELECT SUM(s.totalMargin) FROM Sale s")
    Double sumAllTotalMargin();

    @Query("SELECT SUM(s.totalAmount) FROM Sale s WHERE s.saleDate >= :from AND s.saleDate <= :to")
    Double sumTotalAmountByPeriod(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query("SELECT SUM(s.totalMargin) FROM Sale s WHERE s.saleDate >= :from AND s.saleDate <= :to")
    Double sumTotalMarginByPeriod(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query("""
            SELECT DISTINCT s FROM Sale s
            LEFT JOIN s.items i
            LEFT JOIN i.product p
            WHERE (
                :keyword IS NULL
                OR LOWER(s.customerName) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(s.user.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(s.user.subname) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
            )
            AND (:minAmount IS NULL OR s.totalAmount >= :minAmount)
            AND (:maxAmount IS NULL OR s.totalAmount <= :maxAmount)
            AND (CAST(:startDate AS java.time.LocalDateTime) IS NULL OR s.saleDate >= :startDate)
            AND (CAST(:endDate AS java.time.LocalDateTime) IS NULL OR s.saleDate <= :endDate)
            ORDER BY s.saleDate DESC
            """)
    org.springframework.data.domain.Page<Sale> adminSearchSales(
            @Param("keyword") String keyword,
            @Param("minAmount") Double minAmount,
            @Param("maxAmount") Double maxAmount,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            org.springframework.data.domain.Pageable pageable);

    // ── Top produits agrégés directement en base (évite le N+1) ──────────────

    @Query("""
            SELECT new com.backend.bilanko.DTO.summary.overview.TopSoldProductDTO(
                p.id,
                p.name,
                p.reference,
                SUM(i.quantity),
                SUM(i.unitSellingPrice * i.quantity),
                SUM(i.margin)
            )
            FROM Sale s
            JOIN s.items i
            JOIN i.product p
            WHERE s.user = :user
            GROUP BY p.id, p.name, p.reference
            ORDER BY SUM(i.quantity) DESC
            """)
    List<TopSoldProductDTO> findTopProductsByQuantity(
            @Param("user") User user,
            Pageable pageable);

    @Query("""
            SELECT new com.backend.bilanko.DTO.summary.overview.TopSoldProductDTO(
                p.id,
                p.name,
                p.reference,
                SUM(i.quantity),
                SUM(i.unitSellingPrice * i.quantity),
                SUM(i.margin)
            )
            FROM Sale s
            JOIN s.items i
            JOIN i.product p
            WHERE s.user = :user
            GROUP BY p.id, p.name, p.reference
            ORDER BY SUM(i.unitSellingPrice * i.quantity) DESC
            """)
    List<TopSoldProductDTO> findTopProductsByRevenue(
            @Param("user") User user,
            Pageable pageable);

    @Query("""
            SELECT new com.backend.bilanko.DTO.summary.overview.TopSoldProductDTO(
                p.id,
                p.name,
                p.reference,
                SUM(i.quantity),
                SUM(i.unitSellingPrice * i.quantity),
                SUM(i.margin)
            )
            FROM Sale s
            JOIN s.items i
            JOIN i.product p
            WHERE s.user = :user
            GROUP BY p.id, p.name, p.reference
            ORDER BY SUM(i.margin) DESC
            """)
    List<TopSoldProductDTO> findTopProductsByMargin(
            @Param("user") User user,
            Pageable pageable);

    // ── Avec filtre de dates ──────────────────────────────────────────────────

    @Query("""
            SELECT new com.backend.bilanko.DTO.summary.overview.TopSoldProductDTO(
                p.id,
                p.name,
                p.reference,
                SUM(i.quantity),
                SUM(i.unitSellingPrice * i.quantity),
                SUM(i.margin)
            )
            FROM Sale s
            JOIN s.items i
            JOIN i.product p
            WHERE s.user = :user
              AND s.saleDate >= :from
              AND s.saleDate <= :to
            GROUP BY p.id, p.name, p.reference
            ORDER BY SUM(i.quantity) DESC
            """)
    List<TopSoldProductDTO> findTopProductsByQuantityAndPeriod(
            @Param("user") User user,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            Pageable pageable);

    @Query("""
            SELECT new com.backend.bilanko.DTO.summary.overview.TopSoldProductDTO(
                p.id,
                p.name,
                p.reference,
                SUM(i.quantity),
                SUM(i.unitSellingPrice * i.quantity),
                SUM(i.margin)
            )
            FROM Sale s
            JOIN s.items i
            JOIN i.product p
            WHERE s.user = :user
              AND s.saleDate >= :from
              AND s.saleDate <= :to
            GROUP BY p.id, p.name, p.reference
            ORDER BY SUM(i.unitSellingPrice * i.quantity) DESC
            """)
    List<TopSoldProductDTO> findTopProductsByRevenueAndPeriod(
            @Param("user") User user,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            Pageable pageable);

    @Query("""
            SELECT new com.backend.bilanko.DTO.summary.overview.TopSoldProductDTO(
                p.id,
                p.name,
                p.reference,
                SUM(i.quantity),
                SUM(i.unitSellingPrice * i.quantity),
                SUM(i.margin)
            )
            FROM Sale s
            JOIN s.items i
            JOIN i.product p
            WHERE s.user = :user
              AND s.saleDate >= :from
              AND s.saleDate <= :to
            GROUP BY p.id, p.name, p.reference
            ORDER BY SUM(i.margin) DESC
            """)
    List<TopSoldProductDTO> findTopProductsByMarginAndPeriod(
            @Param("user") User user,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            Pageable pageable);
}
