package com.backend.bilanko.repository.concept.transaction;

import com.backend.bilanko.models.person.User;
import com.backend.bilanko.models.transaction.Sale;
import org.springframework.data.jpa.repository.JpaRepository;

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
}
