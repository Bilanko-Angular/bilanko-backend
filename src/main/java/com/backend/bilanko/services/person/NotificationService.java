package com.backend.bilanko.services.person;

import com.backend.bilanko.DTO.person.notification.NotificationPageDTO;
import com.backend.bilanko.DTO.person.notification.NotificationResponseDTO;
import com.backend.bilanko.mapper.NotificationMapper;
import com.backend.bilanko.models.person.notification.Notification;
import com.backend.bilanko.models.person.notification.NotificationPreferences;
import com.backend.bilanko.models.person.notification.NotificationType;
import com.backend.bilanko.models.person.user.Role;
import com.backend.bilanko.models.person.user.User;
import com.backend.bilanko.repository.person.NotificationRepository;
import com.backend.bilanko.repository.UserRepository;
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
        if (user.getRole() != Role.MERCHANT) {
            return;
        }

        if (!isNotificationEnabledForUser(user, type)) {
            return;
        }

        Notification notification = Notification.builder()
                .user(user)
                .type(type)
                .title(title)
                .message(message)
                .referenceId(referenceId)
                .read(false)
                .build();

        notification = notificationRepository.save(notification);

        long unreadCount = notificationRepository.countByUserAndReadFalse(user);
        
        NotificationResponseDTO dto = NotificationMapper.toDto(notification);
        notificationSseService.push(user.getId(), dto);
        notificationSseService.pushUnreadCount(user.getId(), unreadCount);
    }

    public void broadcastAppUpdate(String title, String message) {
        List<User> merchants = userRepository.findByRole(Role.MERCHANT);
        for (User merchant : merchants) {
            createNotification(merchant, NotificationType.APP_UPDATE, title, message, null);
        }
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
            case WELCOME, NEW_CHARGE -> true; // Toujours valide
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
