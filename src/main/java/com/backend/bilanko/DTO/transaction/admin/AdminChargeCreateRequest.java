package com.backend.bilanko.DTO.transaction.admin;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AdminChargeCreateRequest {
    @NotBlank(message = "Le libellé est obligatoire")
    private String label;
    
    @NotBlank(message = "Le fournisseur est obligatoire")
    private String supplier;
    
    @Min(value = 0, message = "Le montant doit être positif")
    private double amount;
    
    @NotNull(message = "La date est obligatoire")
    private LocalDate date;
    
    private Long categoryId;
    
    @NotNull(message = "L'ID de l'utilisateur est obligatoire")
    private Long userId;
}
