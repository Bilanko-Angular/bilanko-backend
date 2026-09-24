package com.backend.bilanko.repository.person;

import com.backend.bilanko.models.person.notification.Notification;
import com.backend.bilanko.models.person.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Page<Notification> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);

    Optional<Notification> findByIdAndUser(long id, User user);

    long countByUserAndReadFalse(User user);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Notification n SET n.read = true WHERE n.user = :user AND n.read = false")
    int markAllAsRead(@Param("user") User user);
}
