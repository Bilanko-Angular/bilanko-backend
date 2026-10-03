package com.backend.bilanko.DTO.concept.message;

import com.backend.bilanko.models.concept.message.ConversationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SupportConversationDTO {
    private long id;
    private ConversationStatus status;
    private Instant createdAt;
    private Instant updatedAt;

    private long merchantId;
    private String merchantName;
    private String merchantSubname;
    private String merchantEmail;

    /** Admin actuellement en charge (null si en attente). */
    private Long currentAdminId;
    private String currentAdminName;
    private String currentAdminSubname;

    /** true si l'utilisateur courant peut écrire dans cette conversation. */
    private boolean canWrite;

    /** true s'il y a un transfert en attente. */
    private boolean transferPending;
}
