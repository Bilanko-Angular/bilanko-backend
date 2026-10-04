package com.backend.bilanko.models.concept.message;

import com.backend.bilanko.models.BaseEntity;
import com.backend.bilanko.models.person.user.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "support_messages", indexes = {
        @Index(name = "idx_support_msg_conv_created", columnList = "conversation_id, created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupportMessage extends BaseEntity {

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id", nullable = false)
    private SupportConversation conversation;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @Column(nullable = false, length = 2000)
    private String content;
}
