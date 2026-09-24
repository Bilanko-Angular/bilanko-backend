package com.backend.bilanko.DTO.person;

import java.util.List;

public record NotificationPageDTO(
        List<NotificationResponseDTO> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasMore,
        long unreadCount
) {
}
