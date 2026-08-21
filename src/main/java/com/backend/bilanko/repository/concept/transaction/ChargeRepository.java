package com.backend.bilanko.repository.concept.transaction;

import com.backend.bilanko.models.transaction.Charge;
import com.backend.bilanko.models.person.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChargeRepository extends JpaRepository<Charge, Long> {

    List<Charge> findByUserOrderByDateDesc(User user);

    Optional<Charge> findByIdAndUser(long id, User user);
}