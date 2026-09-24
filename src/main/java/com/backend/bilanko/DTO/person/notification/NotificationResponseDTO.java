package com.backend.bilanko.DTO.person.notification;

import com.backend.bilanko.models.person.notification.NotificationType;

import java.time.Instant;

public record NotificationResponseDTO(
        long id,
        NotificationType type,
        String title,
        String message,
        boolean read,
        Long referenceId,
        Instant createdAt
) {
}
