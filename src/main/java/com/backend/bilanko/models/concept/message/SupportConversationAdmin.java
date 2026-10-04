package com.backend.bilanko.models.concept.message;

import com.backend.bilanko.models.BaseEntity;
import com.backend.bilanko.models.person.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * Historique des admins ayant pris part à une conversation.
 * Un admin révoqué conserve la lecture mais ne peut plus écrire.
 */
@Entity
@Table(name = "support_conversation_admins", indexes = {
        @Index(name = "idx_support_conv_admin", columnList = "conversation_id, admin_id", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupportConversationAdmin extends BaseEntity {

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id", nullable = false)
    private SupportConversation conversation;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id", nullable = false)
    private User admin;

    @Column(name = "assigned_at", nullable = false)
    private Instant assignedAt;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    @Builder.Default
    @Column(name = "can_write", nullable = false)
    private boolean canWrite = false;
}
