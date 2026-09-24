package com.backend.bilanko.mapper;

import com.backend.bilanko.DTO.person.notification.NotificationResponseDTO;
import com.backend.bilanko.models.person.Notification;

public final class NotificationMapper {

    private NotificationMapper() {
    }

    public static NotificationResponseDTO toDto(Notification notification) {
        return new NotificationResponseDTO(
                notification.getId(),
                notification.getType(),
                notification.getTitle(),
                notification.getMessage(),
                notification.isRead(),
                notification.getReferenceId(),
                notification.getCreatedAt()
        );
    }
}
