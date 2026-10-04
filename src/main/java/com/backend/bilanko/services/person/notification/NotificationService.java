package com.backend.bilanko.services.person.notification;

import com.backend.bilanko.DTO.person.notification.NotificationPageDTO;
import com.backend.bilanko.DTO.person.notification.NotificationResponseDTO;
import com.backend.bilanko.mapper.NotificationMapper;
import com.backend.bilanko.models.person.notification.Notification;
import com.backend.bilanko.models.person.notification.NotificationPreferences;
import com.backend.bilanko.models.person.notification.NotificationType;
import com.backend.bilanko.models.person.user.Role;
import com.backend.bilanko.models.person.user.User;
import com.backend.bilanko.repository.person.NotificationRepository;
import com.backend.bilanko.repository.person.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationSseService notificationSseService;
    private final UserRepository userRepository;

    @Transactional
    public void createNotification(User user, NotificationType type, String title, String message, Long referenceId) {
        createNotification(user, type, title, message, referenceId, null);
    }

    @Transactional
    public void createNotification(
            User user,
            NotificationType type,
            String title,
            String message,
            Long referenceId,
            String actionLink) {

        if (!canReceive(user, type)) {
            return;
        }

        Notification notification = Notification.builder()
                .user(user)
                .type(type)
                .title(title)
                .message(message)
                .referenceId(referenceId)
                .actionLink(actionLink)
                .read(false)
                .build();

        notification = notificationRepository.save(notification);

        long unreadCount = notificationRepository.countByUserAndReadFalse(user);

        NotificationResponseDTO dto = NotificationMapper.toDto(notification);
        notificationSseService.push(user.getId(), dto);
        notificationSseService.pushUnreadCount(user.getId(), unreadCount);
    }

    /**
     * Supprime les notifications d'un type lié à une ressource, et notifie les clients SSE.
     */
    @Transactional
    public void deleteByTypeAndReferenceId(NotificationType type, Long referenceId) {
        List<Notification> notifications = notificationRepository.findByTypeAndReferenceId(type, referenceId);
        if (notifications.isEmpty()) {
            return;
        }

        for (Notification notification : notifications) {
            User recipient = notification.getUser();
            long userId = recipient.getId();
            long notificationId = notification.getId();
            notificationRepository.delete(notification);
            notificationSseService.pushDeleted(userId, notificationId);
            long unreadCount = notificationRepository.countByUserAndReadFalse(recipient);
            notificationSseService.pushUnreadCount(userId, unreadCount);
        }
    }

    public void broadcastAppUpdate(String title, String message) {
        List<User> merchants = userRepository.findByRole(Role.MERCHANT);
        for (User merchant : merchants) {
            createNotification(merchant, NotificationType.APP_UPDATE, title, message, null);
        }
    }

    private boolean canReceive(User user, NotificationType type) {
        if (isSupportType(type)) {
            return user.getRole() == Role.MERCHANT || user.getRole() == Role.ADMIN;
        }
        // Types métier classiques : MERCHANT uniquement, selon préférences
        if (user.getRole() != Role.MERCHANT) {
            return false;
        }
        return isNotificationEnabledForUser(user, type);
    }

    private boolean isSupportType(NotificationType type) {
        return switch (type) {
            case SUPPORT_CLAIM_REQUEST, SUPPORT_TRANSFER_REQUEST,
                 SUPPORT_CONVERSATION_READY, SUPPORT_ADMIN_CHANGED,
                 SUPPORT_NEW_MESSAGE -> true;
            default -> false;
        };
    }

    private boolean isNotificationEnabledForUser(User user, NotificationType type) {
        NotificationPreferences prefs = user.getNotificationPreferences();
        if (prefs == null) {
            return true;
        }

        return switch (type) {
            case NEW_SALE -> prefs.isNewSales();
            case MONTHLY_REPORT -> prefs.isMonthlyReports();
            case APP_UPDATE -> prefs.isUpdates();
            case WELCOME, NEW_CHARGE,
                 SUPPORT_CLAIM_REQUEST, SUPPORT_TRANSFER_REQUEST,
                 SUPPORT_CONVERSATION_READY, SUPPORT_ADMIN_CHANGED,
                 SUPPORT_NEW_MESSAGE -> true;
        };
    }

    @Transactional(readOnly = true)
    public NotificationPageDTO getUserNotifications(User user, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Notification> notificationPage = notificationRepository.findByUserOrderByCreatedAtDesc(user, pageable);

        List<NotificationResponseDTO> content = notificationPage.getContent().stream()
                .map(NotificationMapper::toDto)
                .collect(Collectors.toList());

        long unreadCount = notificationRepository.countByUserAndReadFalse(user);

        return new NotificationPageDTO(
                content,
                notificationPage.getNumber(),
                notificationPage.getSize(),
                notificationPage.getTotalElements(),
                notificationPage.getTotalPages(),
                notificationPage.hasNext(),
                unreadCount
        );
    }

    @Transactional
    public void markAsRead(long notificationId, User user) {
        notificationRepository.findByIdAndUser(notificationId, user)
                .ifPresent(notification -> {
                    if (!notification.isRead()) {
                        notification.setRead(true);
                        notificationRepository.save(notification);

                        long unreadCount = notificationRepository.countByUserAndReadFalse(user);
                        notificationSseService.pushUnreadCount(user.getId(), unreadCount);
                    }
                });
    }

    @Transactional
    public void markAllAsRead(User user) {
        int updated = notificationRepository.markAllAsRead(user);
        if (updated > 0) {
            notificationSseService.pushUnreadCount(user.getId(), 0);
        }
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(User user) {
        return notificationRepository.countByUserAndReadFalse(user);
    }
}
