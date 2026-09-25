package com.backend.bilanko.repository.person;

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

    long countByActive(boolean active);
    long countByCreatedAtAfter(java.time.Instant date);

    @org.springframework.data.jpa.repository.Query("SELECT u FROM User u WHERE " +
            "(:keyword IS NULL OR LOWER(u.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(u.subname) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(u.phoneNumber) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
            "(:role IS NULL OR u.role = :role) AND " +
            "(:active IS NULL OR u.active = :active)")
    org.springframework.data.domain.Page<User> searchUsers(
            @org.springframework.data.repository.query.Param("keyword") String keyword,
            @org.springframework.data.repository.query.Param("role") Role role,
            @org.springframework.data.repository.query.Param("active") Boolean active,
            org.springframework.data.domain.Pageable pageable);

}
