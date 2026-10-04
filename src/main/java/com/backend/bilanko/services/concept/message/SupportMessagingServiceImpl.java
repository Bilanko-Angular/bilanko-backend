package com.backend.bilanko.services.concept.message;

import com.backend.bilanko.DTO.concept.message.SendMessageRequest;
import com.backend.bilanko.DTO.concept.message.StartConversationRequest;
import com.backend.bilanko.DTO.concept.message.SupportConversationDTO;
import com.backend.bilanko.DTO.concept.message.SupportMessageDTO;
import com.backend.bilanko.DTO.concept.message.TransferConversationRequest;
import com.backend.bilanko.mapper.SupportMessageMapper;
import com.backend.bilanko.models.concept.message.ClaimType;
import com.backend.bilanko.models.concept.message.ConversationStatus;
import com.backend.bilanko.models.concept.message.SupportConversation;
import com.backend.bilanko.models.concept.message.SupportConversationAdmin;
import com.backend.bilanko.models.concept.message.SupportMessage;
import com.backend.bilanko.models.person.notification.NotificationType;
import com.backend.bilanko.models.person.user.Role;
import com.backend.bilanko.models.person.user.User;
import com.backend.bilanko.repository.concept.message.SupportConversationAdminRepository;
import com.backend.bilanko.repository.concept.message.SupportConversationRepository;
import com.backend.bilanko.repository.concept.message.SupportMessageRepository;
import com.backend.bilanko.repository.person.UserRepository;
import com.backend.bilanko.services.person.notification.NotificationService;
import com.backend.bilanko.utils.annotation.AdminOnly;
import com.backend.bilanko.utils.annotation.MerchantOnly;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SupportMessagingServiceImpl implements SupportMessagingService {

    private static final String CLAIM_LINK_PREFIX = "/admin/support/claim/";

    private final SupportConversationRepository conversationRepository;
    private final SupportConversationAdminRepository conversationAdminRepository;
    private final SupportMessageRepository messageRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Override
    @Transactional
    @MerchantOnly
    public SupportConversationDTO startConversation(User merchant, StartConversationRequest request) {
        return conversationRepository.findByMerchant(merchant)
                .map(existing -> {
                    if (existing.getStatus() == ConversationStatus.WAITING_FOR_ADMIN
                            && existing.getPendingClaimToken() != null) {
                        // Conversation déjà en attente : on peut ajouter un message, sans renvoyer les notifs
                        appendInitialMessageIfPresent(existing, merchant, request);
                        return SupportMessageMapper.toConversationDto(existing, true);
                    }
                    if (existing.getStatus() == ConversationStatus.ACTIVE) {
                        return SupportMessageMapper.toConversationDto(existing, true);
                    }
                    // État incohérent (WAITING sans token) : on relance le claim
                    return reopenWaiting(existing, merchant, request);
                })
                .orElseGet(() -> createNewConversation(merchant, request));
    }

    private SupportConversationDTO createNewConversation(User merchant, StartConversationRequest request) {
        String token = newClaimToken();

        SupportConversation conversation = SupportConversation.builder()
                .merchant(merchant)
                .status(ConversationStatus.WAITING_FOR_ADMIN)
                .pendingClaimToken(token)
                .pendingClaimType(ClaimType.INITIAL)
                .build();

        conversation = conversationRepository.save(conversation);

        appendInitialMessageIfPresent(conversation, merchant, request);

        notifyAdminsForInitialClaim(conversation, merchant, token);
        // Le commerçant peut déjà écrire pendant l'attente d'un admin
        return SupportMessageMapper.toConversationDto(conversation, true);
    }

    private SupportConversationDTO reopenWaiting(
            SupportConversation existing, User merchant, StartConversationRequest request) {
        String token = newClaimToken();
        existing.setStatus(ConversationStatus.WAITING_FOR_ADMIN);
        existing.setCurrentAdmin(null);
        existing.setPendingClaimToken(token);
        existing.setPendingClaimType(ClaimType.INITIAL);
        existing.setPendingTransferAdmin(null);
        conversationRepository.save(existing);

        appendInitialMessageIfPresent(existing, merchant, request);

        notificationService.deleteByTypeAndReferenceId(
                NotificationType.SUPPORT_CLAIM_REQUEST, existing.getId());
        notifyAdminsForInitialClaim(existing, merchant, token);
        return SupportMessageMapper.toConversationDto(existing, true);
    }

    private void appendInitialMessageIfPresent(
            SupportConversation conversation, User merchant, StartConversationRequest request) {
        if (request != null && request.getInitialMessage() != null && !request.getInitialMessage().isBlank()) {
            saveMessage(conversation, merchant, request.getInitialMessage().trim());
        }
    }

    private void notifyAdminsForInitialClaim(SupportConversation conversation, User merchant, String token) {
        String merchantLabel = fullName(merchant);
        String actionLink = CLAIM_LINK_PREFIX + token;
        List<User> admins = userRepository.findByRole(Role.ADMIN);

        for (User admin : admins) {
            notificationService.createNotification(
                    admin,
                    NotificationType.SUPPORT_CLAIM_REQUEST,
                    "Nouvelle demande de support",
                    merchantLabel + " a démarré une conversation avec le service client. Cliquez pour prendre la main.",
                    conversation.getId(),
                    actionLink
            );
        }
    }

    @Override
    @Transactional(readOnly = true)
    @MerchantOnly
    public SupportConversationDTO getMyConversation(User merchant) {
        SupportConversation conversation = conversationRepository.findByMerchant(merchant)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Aucune conversation de support"));
        return SupportMessageMapper.toConversationDto(conversation, canWrite(merchant, conversation));
    }

    @Override
    @Transactional(readOnly = true)
    @AdminOnly
    public Page<SupportConversationDTO> getAdminConversations(User admin, int page, int size) {
        Pageable pageable = PageRequest.of(page, size,
                org.springframework.data.domain.Sort.by(
                        org.springframework.data.domain.Sort.Direction.DESC, "conversation.updatedAt"));
        return conversationAdminRepository.findByAdmin(admin, pageable)
                .map(participation -> SupportMessageMapper.toConversationDto(
                        participation.getConversation(),
                        participation.isCanWrite()
                                && participation.getConversation().getStatus() == ConversationStatus.ACTIVE
                                && participation.getConversation().getCurrentAdmin() != null
                                && participation.getConversation().getCurrentAdmin().getId() == admin.getId()
                ));
    }

    @Override
    @Transactional(readOnly = true)
    public SupportConversationDTO getConversation(User currentUser, long conversationId) {
        SupportConversation conversation = findConversationOrThrow(conversationId);
        assertCanRead(currentUser, conversation);
        return SupportMessageMapper.toConversationDto(conversation, canWrite(currentUser, conversation));
    }

    @Override
    @Transactional
    @AdminOnly
    public SupportConversationDTO claimConversation(User admin, String token) {
        if (token == null || token.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Token de prise en main invalide");
        }

        SupportConversation conversation = conversationRepository.findByPendingClaimToken(token.trim())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.GONE,
                        "Ce lien de prise en main n'est plus valide"));

        if (conversation.getPendingClaimType() == ClaimType.INITIAL) {
            return claimInitial(admin, conversation, token.trim());
        }
        if (conversation.getPendingClaimType() == ClaimType.TRANSFER) {
            return claimTransfer(admin, conversation, token.trim());
        }

        throw new ResponseStatusException(HttpStatus.GONE, "Ce lien de prise en main n'est plus valide");
    }

    private SupportConversationDTO claimInitial(User admin, SupportConversation conversation, String token) {
        int updated = conversationRepository.claimInitial(conversation.getId(), token, admin);
        if (updated == 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "La conversation a déjà été prise en charge par un autre admin");
        }

        // Recharge l'entité après UPDATE JPQL
        SupportConversation claimed = findConversationOrThrow(conversation.getId());
        upsertAdminParticipation(claimed, admin, true);

        notificationService.deleteByTypeAndReferenceId(
                NotificationType.SUPPORT_CLAIM_REQUEST, claimed.getId());

        notificationService.createNotification(
                claimed.getMerchant(),
                NotificationType.SUPPORT_CONVERSATION_READY,
                "Conversation prise en charge",
                "Votre conversation peut commencer. Vous discutez avec " + fullName(admin) + ".",
                claimed.getId()
        );

        return SupportMessageMapper.toConversationDto(claimed, true);
    }

    private SupportConversationDTO claimTransfer(User admin, SupportConversation conversation, String token) {
        if (conversation.getPendingTransferAdmin() == null
                || conversation.getPendingTransferAdmin().getId() != admin.getId()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Ce transfert ne vous est pas destiné");
        }

        User previousAdmin = conversation.getCurrentAdmin();

        int updated = conversationRepository.claimTransfer(conversation.getId(), token, admin);
        if (updated == 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Ce transfert n'est plus valide ou a déjà été accepté");
        }

        SupportConversation claimed = findConversationOrThrow(conversation.getId());

        if (previousAdmin != null) {
            conversationAdminRepository.findByConversationIdAndAdminId(claimed.getId(), previousAdmin.getId())
                    .ifPresent(participation -> {
                        participation.setCanWrite(false);
                        participation.setRevokedAt(Instant.now());
                        conversationAdminRepository.save(participation);
                    });
        }

        upsertAdminParticipation(claimed, admin, true);

        notificationService.deleteByTypeAndReferenceId(
                NotificationType.SUPPORT_TRANSFER_REQUEST, claimed.getId());

        notificationService.createNotification(
                claimed.getMerchant(),
                NotificationType.SUPPORT_ADMIN_CHANGED,
                "Changement d'interlocuteur",
                "Votre conversation est désormais prise en charge par " + fullName(admin) + ".",
                claimed.getId()
        );

        return SupportMessageMapper.toConversationDto(claimed, true);
    }

    @Override
    @Transactional
    @AdminOnly
    public SupportConversationDTO transferConversation(
            User admin, long conversationId, TransferConversationRequest request) {

        SupportConversation conversation = findConversationOrThrow(conversationId);

        if (conversation.getStatus() != ConversationStatus.ACTIVE
                || conversation.getCurrentAdmin() == null
                || conversation.getCurrentAdmin().getId() != admin.getId()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Seul l'admin en charge peut transférer cette conversation");
        }

        if (conversation.getPendingClaimType() == ClaimType.TRANSFER
                && conversation.getPendingClaimToken() != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Un transfert est déjà en attente pour cette conversation");
        }

        User targetAdmin = userRepository.findByEmailIgnoreCase(request.getAdminEmail().trim())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Aucun admin trouvé avec cet email"));

        if (targetAdmin.getRole() != Role.ADMIN) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "L'utilisateur cible doit être un administrateur");
        }
        if (targetAdmin.getId() == admin.getId()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Vous ne pouvez pas vous transférer la conversation à vous-même");
        }

        String token = newClaimToken();
        conversation.setPendingClaimToken(token);
        conversation.setPendingClaimType(ClaimType.TRANSFER);
        conversation.setPendingTransferAdmin(targetAdmin);
        conversationRepository.save(conversation);

        String actionLink = CLAIM_LINK_PREFIX + token;
        notificationService.createNotification(
                targetAdmin,
                NotificationType.SUPPORT_TRANSFER_REQUEST,
                "Transfert de conversation",
                fullName(admin) + " vous propose de reprendre la conversation avec "
                        + fullName(conversation.getMerchant()) + ". Cliquez pour accepter.",
                conversation.getId(),
                actionLink
        );

        return SupportMessageMapper.toConversationDto(conversation, true);
    }

    @Override
    @Transactional
    public SupportMessageDTO sendMessage(User currentUser, long conversationId, SendMessageRequest request) {
        SupportConversation conversation = findConversationOrThrow(conversationId);
        assertCanRead(currentUser, conversation);

        if (!canWrite(currentUser, conversation)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Vous ne pouvez plus envoyer de messages dans cette conversation");
        }

        SupportMessage saved = saveMessage(conversation, currentUser, request.getContent().trim());
        notifyNewMessage(conversation, currentUser, saved);
        return SupportMessageMapper.toMessageDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SupportMessageDTO> getMessages(User currentUser, long conversationId, int page, int size) {
        SupportConversation conversation = findConversationOrThrow(conversationId);
        assertCanRead(currentUser, conversation);

        Pageable pageable = PageRequest.of(page, size);
        return messageRepository.findByConversationIdOrderByCreatedAtDesc(conversationId, pageable)
                .map(SupportMessageMapper::toMessageDto);
    }

    // ── Helpers ──────────────────────────────────────────────────

    private SupportMessage saveMessage(SupportConversation conversation, User sender, String content) {
        SupportMessage message = SupportMessage.builder()
                .conversation(conversation)
                .sender(sender)
                .content(content)
                .build();
        SupportMessage saved = messageRepository.save(message);
        // Force le dirty-check pour mettre à jour updatedAt (tri liste admin)
        conversation.setUpdatedAt(Instant.now());
        conversationRepository.save(conversation);
        return saved;
    }

    private void notifyNewMessage(SupportConversation conversation, User sender, SupportMessage message) {
        String preview = message.getContent().length() > 80
                ? message.getContent().substring(0, 77) + "..."
                : message.getContent();

        if (sender.getRole() == Role.MERCHANT && conversation.getCurrentAdmin() != null) {
            notificationService.createNotification(
                    conversation.getCurrentAdmin(),
                    NotificationType.SUPPORT_NEW_MESSAGE,
                    "Nouveau message support",
                    fullName(sender) + " : " + preview,
                    conversation.getId()
            );
        } else if (sender.getRole() == Role.ADMIN) {
            notificationService.createNotification(
                    conversation.getMerchant(),
                    NotificationType.SUPPORT_NEW_MESSAGE,
                    "Nouveau message du support",
                    fullName(sender) + " : " + preview,
                    conversation.getId()
            );
        }
    }

    private void upsertAdminParticipation(SupportConversation conversation, User admin, boolean canWrite) {
        SupportConversationAdmin participation = conversationAdminRepository
                .findByConversationIdAndAdminId(conversation.getId(), admin.getId())
                .orElseGet(() -> SupportConversationAdmin.builder()
                        .conversation(conversation)
                        .admin(admin)
                        .assignedAt(Instant.now())
                        .build());

        participation.setCanWrite(canWrite);
        participation.setRevokedAt(canWrite ? null : Instant.now());
        if (participation.getAssignedAt() == null) {
            participation.setAssignedAt(Instant.now());
        }
        // Si l'admin revient après un transfert précédent, on remet assignedAt au reprise
        if (canWrite) {
            participation.setAssignedAt(Instant.now());
            participation.setRevokedAt(null);
        }
        conversationAdminRepository.save(participation);
    }

    private SupportConversation findConversationOrThrow(long id) {
        return conversationRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Conversation introuvable : id=" + id));
    }

    private void assertCanRead(User user, SupportConversation conversation) {
        if (user.getRole() == Role.MERCHANT) {
            if (conversation.getMerchant().getId() != user.getId()) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Accès refusé à cette conversation");
            }
            return;
        }
        if (user.getRole() == Role.ADMIN) {
            // Historique uniquement après prise en main (participant) ou admin courant
            boolean isParticipant = conversationAdminRepository
                    .existsByConversationIdAndAdminId(conversation.getId(), user.getId());
            boolean isCurrent = conversation.getCurrentAdmin() != null
                    && conversation.getCurrentAdmin().getId() == user.getId();
            if (!isParticipant && !isCurrent) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Accès refusé à cette conversation");
            }
            return;
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Accès refusé");
    }

    private boolean canWrite(User user, SupportConversation conversation) {
        if (user.getRole() == Role.MERCHANT) {
            if (conversation.getMerchant().getId() != user.getId()) {
                return false;
            }
            // Peut écrire en attendant un admin, ou une fois la conversation ACTIVE
            return conversation.getStatus() == ConversationStatus.WAITING_FOR_ADMIN
                    || conversation.getStatus() == ConversationStatus.ACTIVE;
        }
        if (user.getRole() == Role.ADMIN) {
            return conversation.getStatus() == ConversationStatus.ACTIVE
                    && conversation.getCurrentAdmin() != null
                    && conversation.getCurrentAdmin().getId() == user.getId();
        }
        return false;
    }

    private String newClaimToken() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    private String fullName(User user) {
        String name = user.getName() != null ? user.getName().trim() : "";
        String subname = user.getSubname() != null ? user.getSubname().trim() : "";
        if (subname.isBlank()) {
            return name.isBlank() ? user.getEmail() : name;
        }
        if (name.isBlank()) {
            return subname;
        }
        return name + " " + subname;
    }
}
