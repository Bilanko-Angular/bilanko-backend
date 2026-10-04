package com.backend.bilanko.services.concept.message;

import com.backend.bilanko.DTO.concept.message.SendMessageRequest;
import com.backend.bilanko.DTO.concept.message.StartConversationRequest;
import com.backend.bilanko.DTO.concept.message.SupportConversationDTO;
import com.backend.bilanko.DTO.concept.message.SupportMessageDTO;
import com.backend.bilanko.DTO.concept.message.TransferConversationRequest;
import com.backend.bilanko.models.person.user.User;
import org.springframework.data.domain.Page;

public interface SupportMessagingService {

    SupportConversationDTO startConversation(User merchant, StartConversationRequest request);

    SupportConversationDTO getMyConversation(User merchant);

    Page<SupportConversationDTO> getAdminConversations(User admin, int page, int size);

    SupportConversationDTO getConversation(User currentUser, long conversationId);

    SupportConversationDTO claimConversation(User admin, String token);

    SupportConversationDTO transferConversation(User admin, long conversationId, TransferConversationRequest request);

    SupportMessageDTO sendMessage(User currentUser, long conversationId, SendMessageRequest request);

    Page<SupportMessageDTO> getMessages(User currentUser, long conversationId, int page, int size);
}
