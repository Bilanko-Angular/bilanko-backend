package com.backend.bilanko.controller.concept.message;

import com.backend.bilanko.DTO.concept.message.SendMessageRequest;
import com.backend.bilanko.DTO.concept.message.StartConversationRequest;
import com.backend.bilanko.DTO.concept.message.SupportConversationDTO;
import com.backend.bilanko.DTO.concept.message.SupportMessageDTO;
import com.backend.bilanko.DTO.concept.message.TransferConversationRequest;
import com.backend.bilanko.models.person.user.User;
import com.backend.bilanko.services.concept.message.SupportMessagingService;
import com.backend.bilanko.utils.routes.SupportMessagingApiRoutes;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(SupportMessagingApiRoutes.BASE)
@RequiredArgsConstructor
public class SupportMessagingController {

    private final SupportMessagingService supportMessagingService;

    /** MERCHANT : démarre (ou récupère) sa conversation unique avec le support. */
    @PostMapping(SupportMessagingApiRoutes.START)
    public ResponseEntity<SupportConversationDTO> startConversation(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody(required = false) StartConversationRequest request) {
        StartConversationRequest body = request != null ? request : new StartConversationRequest();
        return ResponseEntity.ok(supportMessagingService.startConversation(currentUser, body));
    }

    /** MERCHANT : récupère sa conversation (avec nom/prénom de l'admin en charge). */
    @GetMapping(SupportMessagingApiRoutes.MY_CONVERSATION)
    public ResponseEntity<SupportConversationDTO> getMyConversation(
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(supportMessagingService.getMyConversation(currentUser));
    }

    /** ADMIN : liste paginée de ses conversations (actuelles + historiques). */
    @GetMapping(SupportMessagingApiRoutes.ADMIN_CONVERSATIONS)
    public ResponseEntity<Page<SupportConversationDTO>> getAdminConversations(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(supportMessagingService.getAdminConversations(currentUser, page, size));
    }

    /** Détail d'une conversation (merchant propriétaire ou admin participant). */
    @GetMapping(SupportMessagingApiRoutes.CONVERSATION_BY_ID)
    public ResponseEntity<SupportConversationDTO> getConversation(
            @AuthenticationPrincipal User currentUser,
            @PathVariable long id) {
        return ResponseEntity.ok(supportMessagingService.getConversation(currentUser, id));
    }

    /** ADMIN : prend la main via le token du lien de notification. */
    @PostMapping(SupportMessagingApiRoutes.CLAIM)
    public ResponseEntity<SupportConversationDTO> claimConversation(
            @AuthenticationPrincipal User currentUser,
            @PathVariable String token) {
        return ResponseEntity.ok(supportMessagingService.claimConversation(currentUser, token));
    }

    /** ADMIN : demande un transfert vers un autre admin (par email). */
    @PostMapping(SupportMessagingApiRoutes.TRANSFER)
    public ResponseEntity<SupportConversationDTO> transferConversation(
            @AuthenticationPrincipal User currentUser,
            @PathVariable long id,
            @Valid @RequestBody TransferConversationRequest request) {
        return ResponseEntity.ok(supportMessagingService.transferConversation(currentUser, id, request));
    }

    /** Envoi d'un message (merchant si ACTIVE, admin si en charge). */
    @PostMapping(SupportMessagingApiRoutes.MESSAGES)
    public ResponseEntity<SupportMessageDTO> sendMessage(
            @AuthenticationPrincipal User currentUser,
            @PathVariable long id,
            @Valid @RequestBody SendMessageRequest request) {
        return ResponseEntity.ok(supportMessagingService.sendMessage(currentUser, id, request));
    }

    /** Liste paginée des messages (du plus récent au plus ancien). */
    @GetMapping(SupportMessagingApiRoutes.MESSAGES)
    public ResponseEntity<Page<SupportMessageDTO>> getMessages(
            @AuthenticationPrincipal User currentUser,
            @PathVariable long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(supportMessagingService.getMessages(currentUser, id, page, size));
    }
}
