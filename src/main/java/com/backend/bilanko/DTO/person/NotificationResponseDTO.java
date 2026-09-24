package com.backend.bilanko.DTO.person;

import com.backend.bilanko.models.person.NotificationType;

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
