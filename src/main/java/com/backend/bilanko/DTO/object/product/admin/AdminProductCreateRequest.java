package com.backend.bilanko.DTO.object.product.admin;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AdminProductCreateRequest {

    @NotNull(message = "L'ID de l'utilisateur est obligatoire")
    private Long userId;

    @NotBlank(message = "Le nom du produit est obligatoire")
    private String name;

    @Min(value = 0, message = "La quantité doit être positive ou nulle")
    private int quantity;

    @Min(value = 0, message = "Le prix de vente doit être positif ou nul")
    private double price;

    @Min(value = 0, message = "Le prix d'achat doit être positif ou nul")
    private double purchasePrice;

    @Min(value = 0, message = "Le seuil d'alerte doit être positif ou nul")
    private Integer alertThreshold;

    private List<Long> categoryIds;
}
