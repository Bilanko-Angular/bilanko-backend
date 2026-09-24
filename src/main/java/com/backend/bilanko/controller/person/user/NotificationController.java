package com.backend.bilanko.controller.person.user;

import com.backend.bilanko.DTO.person.notification.AppUpdateBroadcastRequest;
import com.backend.bilanko.DTO.person.notification.NotificationPageDTO;
import com.backend.bilanko.models.person.User;
import com.backend.bilanko.services.person.NotificationService;
import com.backend.bilanko.services.person.NotificationSseService;
import com.backend.bilanko.utils.routes.NotificationApiRoutes;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final NotificationSseService notificationSseService;

    @GetMapping(path = NotificationApiRoutes.STREAM, produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamNotifications(@AuthenticationPrincipal User user) {
        return notificationSseService.subscribe(user.getId());
    }

    @GetMapping(NotificationApiRoutes.BASE)
    public ResponseEntity<NotificationPageDTO> getNotifications(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        NotificationPageDTO result = notificationService.getUserNotifications(user, page, size);
        return ResponseEntity.ok(result);
    }

    @PutMapping(NotificationApiRoutes.MARK_READ)
    public ResponseEntity<Void> markAsRead(
            @PathVariable long id,
            @AuthenticationPrincipal User user) {
        notificationService.markAsRead(id, user);
        return ResponseEntity.noContent().build();
    }

    @PutMapping(NotificationApiRoutes.MARK_ALL_READ)
    public ResponseEntity<Void> markAllAsRead(@AuthenticationPrincipal User user) {
        notificationService.markAllAsRead(user);
        return ResponseEntity.noContent().build();
    }

    @GetMapping(NotificationApiRoutes.UNREAD_COUNT)
    public ResponseEntity<Map<String, Long>> getUnreadCount(@AuthenticationPrincipal User user) {
        long count = notificationService.getUnreadCount(user);
        return ResponseEntity.ok(Map.of("count", count));
    }

    @PostMapping(NotificationApiRoutes.BROADCAST_APP_UPDATE)
    public ResponseEntity<Void> broadcastAppUpdate(@Valid @RequestBody AppUpdateBroadcastRequest request) {
        notificationService.broadcastAppUpdate(request.title(), request.message());
        return ResponseEntity.ok().build();
    }
}
