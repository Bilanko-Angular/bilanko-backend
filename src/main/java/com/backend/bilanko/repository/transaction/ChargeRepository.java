package com.backend.bilanko.repository.transaction;

import com.backend.bilanko.models.transaction.Charge;
import com.backend.bilanko.models.person.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    @Query("SELECT c FROM Charge c WHERE " +
            "(:keyword IS NULL OR LOWER(c.label) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(c.supplier) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(c.user.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(c.user.subname) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
            "(:categoryId IS NULL OR c.chargeCategory.id = :categoryId) AND " +
            "(CAST(:startDate AS date) IS NULL OR c.date >= :startDate) AND " +
            "(CAST(:endDate AS date) IS NULL OR c.date <= :endDate)")
    org.springframework.data.domain.Page<Charge> adminSearchCharges(
            @Param("keyword") String keyword,
            @Param("categoryId") Long categoryId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            org.springframework.data.domain.Pageable pageable);

}