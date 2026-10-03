package com.backend.bilanko.mapper;

import com.backend.bilanko.DTO.concept.message.SupportConversationDTO;
import com.backend.bilanko.DTO.concept.message.SupportMessageDTO;
import com.backend.bilanko.models.concept.message.ClaimType;
import com.backend.bilanko.models.concept.message.SupportConversation;
import com.backend.bilanko.models.concept.message.SupportMessage;
import com.backend.bilanko.models.person.user.User;

public final class SupportMessageMapper {

    private SupportMessageMapper() {
    }

    public static SupportConversationDTO toConversationDto(SupportConversation conversation, boolean canWrite) {
        User merchant = conversation.getMerchant();
        User admin = conversation.getCurrentAdmin();

        SupportConversationDTO.SupportConversationDTOBuilder builder = SupportConversationDTO.builder()
                .id(conversation.getId())
                .status(conversation.getStatus())
                .createdAt(conversation.getCreatedAt())
                .updatedAt(conversation.getUpdatedAt())
                .merchantId(merchant.getId())
                .merchantName(merchant.getName())
                .merchantSubname(merchant.getSubname())
                .merchantEmail(merchant.getEmail())
                .canWrite(canWrite)
                .transferPending(conversation.getPendingClaimType() == ClaimType.TRANSFER
                        && conversation.getPendingClaimToken() != null);

        if (admin != null) {
            builder.currentAdminId(admin.getId())
                    .currentAdminName(admin.getName())
                    .currentAdminSubname(admin.getSubname());
        }

        return builder.build();
    }

    public static SupportMessageDTO toMessageDto(SupportMessage message) {
        User sender = message.getSender();
        return SupportMessageDTO.builder()
                .id(message.getId())
                .content(message.getContent())
                .createdAt(message.getCreatedAt())
                .senderId(sender.getId())
                .senderName(sender.getName())
                .senderSubname(sender.getSubname())
                .senderRole(sender.getRole().name())
                .build();
    }
}
