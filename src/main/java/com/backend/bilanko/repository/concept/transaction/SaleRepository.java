package com.backend.bilanko.repository.concept.transaction;

import com.backend.bilanko.models.person.User;
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
}
