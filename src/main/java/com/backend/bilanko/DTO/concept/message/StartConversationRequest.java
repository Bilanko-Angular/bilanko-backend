package com.backend.bilanko.DTO.concept.message;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StartConversationRequest {

    @Size(max = 2000, message = "Le message initial ne peut pas dépasser 2000 caractères")
    private String initialMessage;
}
