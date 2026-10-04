package com.backend.bilanko.repository.concept.message;

import com.backend.bilanko.models.concept.message.SupportConversationAdmin;
import com.backend.bilanko.models.person.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SupportConversationAdminRepository extends JpaRepository<SupportConversationAdmin, Long> {

    Optional<SupportConversationAdmin> findByConversationIdAndAdminId(long conversationId, long adminId);

    List<SupportConversationAdmin> findByConversationIdOrderByAssignedAtAsc(long conversationId);

    @Query("""
            SELECT sca FROM SupportConversationAdmin sca
            JOIN FETCH sca.conversation c
            JOIN FETCH c.merchant
            LEFT JOIN FETCH c.currentAdmin
            WHERE sca.admin = :admin
            ORDER BY c.updatedAt DESC
            """)
    List<SupportConversationAdmin> findAllByAdminWithConversation(@Param("admin") User admin);

    @Query("""
            SELECT sca FROM SupportConversationAdmin sca
            JOIN sca.conversation c
            WHERE sca.admin = :admin
            """)
    Page<SupportConversationAdmin> findByAdmin(
            @Param("admin") User admin, Pageable pageable);

    boolean existsByConversationIdAndAdminId(long conversationId, long adminId);
}
