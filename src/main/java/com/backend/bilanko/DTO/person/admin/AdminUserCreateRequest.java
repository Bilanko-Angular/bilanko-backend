package com.backend.bilanko.DTO.person.admin;

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
public class AdminUserCreateRequest {
    @NotBlank(message = "Le nom est obligatoire")
    private String name;
    
    @NotBlank(message = "Le prénom est obligatoire")
    private String subname;
    
    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "L'email doit être valide")
    private String email;
    
    @NotBlank(message = "Le mot de passe est obligatoire")
    private String password;
}
