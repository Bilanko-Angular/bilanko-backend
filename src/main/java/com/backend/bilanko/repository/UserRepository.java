package com.backend.bilanko.repository;

import com.backend.bilanko.models.person.user.User;
import com.backend.bilanko.models.person.user.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public interface UserRepository extends JpaRepository<User,Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsById(long id);
    java.util.List<User> findByRole(Role role);

}
