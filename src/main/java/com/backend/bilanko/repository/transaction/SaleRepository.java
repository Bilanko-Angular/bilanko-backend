package com.backend.bilanko.repository.transaction;

import com.backend.bilanko.models.person.user.User;
import com.backend.bilanko.models.transaction.Sale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SaleRepository extends JpaRepository<Sale, Long> {
    List<Sale> findByUserOrderBySaleDateDesc(User user);

    List<Sale> findByUserAndSaleDateBetweenOrderBySaleDateDesc(
            User user,
            LocalDateTime from,
            LocalDateTime to
    );

    Optional<Sale> findByIdAndUser(long id, User user);

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

    @Query("SELECT SUM(s.totalAmount) FROM Sale s")
    Double sumAllTotalAmount();

    @Query("SELECT SUM(s.totalMargin) FROM Sale s")
    Double sumAllTotalMargin();

    @Query("SELECT SUM(s.totalAmount) FROM Sale s WHERE s.saleDate >= :from AND s.saleDate <= :to")
    Double sumTotalAmountByPeriod(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

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
}
