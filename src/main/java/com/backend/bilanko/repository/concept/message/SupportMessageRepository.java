package com.backend.bilanko.repository.concept.message;

import com.backend.bilanko.models.concept.message.SupportMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SupportMessageRepository extends JpaRepository<SupportMessage, Long> {

    Page<SupportMessage> findByConversationIdOrderByCreatedAtAsc(long conversationId, Pageable pageable);

    Page<SupportMessage> findByConversationIdOrderByCreatedAtDesc(long conversationId, Pageable pageable);
}
