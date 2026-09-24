package com.backend.bilanko.models.person.notification;

import com.backend.bilanko.models.BaseEntity;
import com.backend.bilanko.models.person.user.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "notifications", indexes = {
        @Index(name = "idx_notifications_user_created", columnList = "user_id, created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification extends BaseEntity {

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private NotificationType type;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, length = 1000)
    private String message;

    @Builder.Default
    @Column(name = "is_read", nullable = false)
    private boolean read = false;

    /** Identifiant optionnel de la ressource liée (vente, charge…). */
    @Column(name = "reference_id")
    private Long referenceId;
}
