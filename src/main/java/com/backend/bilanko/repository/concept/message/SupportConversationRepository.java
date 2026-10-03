package com.backend.bilanko.repository.concept.message;

import com.backend.bilanko.models.concept.message.SupportConversation;
import com.backend.bilanko.models.person.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface SupportConversationRepository extends JpaRepository<SupportConversation, Long> {

    Optional<SupportConversation> findByMerchant(User merchant);

    Optional<SupportConversation> findByMerchantId(long merchantId);

    Optional<SupportConversation> findByPendingClaimToken(String pendingClaimToken);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE SupportConversation c
            SET c.currentAdmin = :admin,
                c.status = com.backend.bilanko.models.concept.message.ConversationStatus.ACTIVE,
                c.pendingClaimToken = null,
                c.pendingClaimType = null,
                c.pendingTransferAdmin = null
            WHERE c.id = :conversationId
              AND c.pendingClaimToken = :token
              AND c.status = com.backend.bilanko.models.concept.message.ConversationStatus.WAITING_FOR_ADMIN
              AND c.pendingClaimType = com.backend.bilanko.models.concept.message.ClaimType.INITIAL
            """)
    int claimInitial(
            @Param("conversationId") long conversationId,
            @Param("token") String token,
            @Param("admin") User admin);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE SupportConversation c
            SET c.currentAdmin = :admin,
                c.pendingClaimToken = null,
                c.pendingClaimType = null,
                c.pendingTransferAdmin = null
            WHERE c.id = :conversationId
              AND c.pendingClaimToken = :token
              AND c.pendingClaimType = com.backend.bilanko.models.concept.message.ClaimType.TRANSFER
              AND c.pendingTransferAdmin = :admin
              AND c.status = com.backend.bilanko.models.concept.message.ConversationStatus.ACTIVE
            """)
    int claimTransfer(
            @Param("conversationId") long conversationId,
            @Param("token") String token,
            @Param("admin") User admin);
}
