package com.backend.bilanko.DTO.concept.message;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TransferConversationRequest {

    @NotBlank(message = "L'email de l'admin est obligatoire")
    @Email(message = "Email invalide")
    private String adminEmail;
}
