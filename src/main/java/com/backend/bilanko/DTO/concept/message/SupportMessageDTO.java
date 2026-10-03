package com.backend.bilanko.DTO.concept.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SupportMessageDTO {
    private long id;
    private String content;
    private Instant createdAt;
    private long senderId;
    private String senderName;
    private String senderSubname;
    private String senderRole;
}
