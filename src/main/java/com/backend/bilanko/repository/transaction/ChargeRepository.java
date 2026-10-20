package com.backend.bilanko.repository.transaction;

import com.backend.bilanko.models.transaction.Charge;
import com.backend.bilanko.models.person.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ChargeRepository extends JpaRepository<Charge, Long> {

    List<Charge> findByUserOrderByDateDesc(User user);

    List<Charge> findByUserAndDateBetweenOrderByDateDesc(User user, LocalDate from, LocalDate to);

    Optional<Charge> findByIdAndUser(long id, User user);

    @Query("SELECT SUM(c.amount) FROM Charge c")
    Double sumAllCharges();

    @Query("SELECT SUM(c.amount) FROM Charge c WHERE c.date >= :startDate AND c.date <= :endDate")
    Double sumChargesByPeriod(@Param("startDate") LocalDate startDate, @org.springframework.data.repository.query.Param("endDate") LocalDate endDate);

    @Query("SELECT AVG(c.amount) FROM Charge c")
    Double averageChargeAmount();

    List<Charge> findByDateBetween(LocalDate from, LocalDate to);

    @Query("""
            SELECT c.supplier, SUM(c.amount), COUNT(c)
            FROM Charge c
            GROUP BY c.supplier
            ORDER BY SUM(c.amount) DESC
            """)
    List<Object[]> findSupplierShares(Pageable pageable);

    @Query("SELECT c FROM Charge c WHERE " +
            "(:keyword IS NULL OR LOWER(c.label) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(c.supplier) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(c.user.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(c.user.subname) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
            "(:categoryId IS NULL OR c.chargeCategory.id = :categoryId) AND " +
            "(CAST(:startDate AS date) IS NULL OR c.date >= :startDate) AND " +
            "(CAST(:endDate AS date) IS NULL OR c.date <= :endDate)")
    Page<Charge> adminSearchCharges(
            @Param("keyword") String keyword,
            @Param("categoryId") Long categoryId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            org.springframework.data.domain.Pageable pageable);

    // ---------------------------------------------------------------
    // Merchant : liste paginée
    // ---------------------------------------------------------------
    Page<Charge> findByUserOrderByDateDesc(User user, Pageable pageable);

    Page<Charge> findByUserAndDateBetweenOrderByDateDesc(User user, LocalDate from, LocalDate to, Pageable pageable);

    // ---------------------------------------------------------------
    // Merchant : recherche avec filtres optionnels
    // ---------------------------------------------------------------
    @Query("""
            SELECT c FROM Charge c
            WHERE c.user = :user
              AND (:label     IS NULL OR LOWER(c.label)    LIKE LOWER(CONCAT('%', :label,    '%')))
              AND (:supplier  IS NULL OR LOWER(c.supplier) LIKE LOWER(CONCAT('%', :supplier, '%')))
              AND (:minAmount IS NULL OR c.amount >= :minAmount)
              AND (:maxAmount IS NULL OR c.amount <= :maxAmount)
              AND (CAST(:date AS date) IS NULL OR c.date = :date)
            ORDER BY c.date DESC
            """)
    Page<Charge> searchByUser(
            @Param("user")       User user,
            @Param("label")      String label,
            @Param("supplier")   String supplier,
            @Param("minAmount")  BigDecimal minAmount,
            @Param("maxAmount")  BigDecimal maxAmount,
            @Param("date")       LocalDate date,
            Pageable pageable);

    // ---------------------------------------------------------------
    // Recherche globale : filtre par keyword directement en BDD
    // + JOIN FETCH de chargeCategory pour éviter le N+1
    // ---------------------------------------------------------------
    @Query("""
            SELECT c FROM Charge c
            LEFT JOIN FETCH c.chargeCategory
            WHERE c.user = :user
            AND (
                LOWER(c.label)    LIKE LOWER(CONCAT('%', :kw, '%'))
             OR LOWER(c.supplier) LIKE LOWER(CONCAT('%', :kw, '%'))
             OR (c.chargeCategory IS NOT NULL
                 AND LOWER(c.chargeCategory.name) LIKE LOWER(CONCAT('%', :kw, '%')))
            )
            ORDER BY c.date DESC
            """)
    List<Charge> searchByUserAndKeyword(
            @Param("user") User user,
            @Param("kw")   String keyword,
            Pageable pageable);

}