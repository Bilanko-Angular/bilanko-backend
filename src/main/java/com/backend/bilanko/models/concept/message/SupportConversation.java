package com.backend.bilanko.models.concept.message;

import com.backend.bilanko.models.BaseEntity;
import com.backend.bilanko.models.person.user.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "support_conversations", indexes = {
        @Index(name = "idx_support_conv_merchant", columnList = "merchant_id", unique = true),
        @Index(name = "idx_support_conv_claim_token", columnList = "pending_claim_token")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupportConversation extends BaseEntity {

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "merchant_id", nullable = false, unique = true)
    private User merchant;

    /** Admin actuellement responsable (null tant que personne n'a pris la main). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_admin_id")
    private User currentAdmin;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ConversationStatus status;

    /** Token du lien de prise en main (initial ou transfert). Null = aucun claim en cours. */
    @Column(name = "pending_claim_token", length = 64)
    private String pendingClaimToken;

    @Enumerated(EnumType.STRING)
    @Column(name = "pending_claim_type", length = 20)
    private ClaimType pendingClaimType;

    /** Cible d'un transfert en attente (uniquement si pendingClaimType = TRANSFER). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pending_transfer_admin_id")
    private User pendingTransferAdmin;
}
