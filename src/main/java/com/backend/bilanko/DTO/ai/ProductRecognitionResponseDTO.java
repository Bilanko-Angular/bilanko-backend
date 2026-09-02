package com.backend.bilanko.DTO.ai;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.util.List;

public record ProductRecognitionResponseDTO(
        @JsonPropertyDescription("Nom suggéré du produit, ou null si incertain")
        String suggestedName,

        @JsonPropertyDescription("Catégories parmi celles déjà existantes qui correspondent, peut être vide")
        List<String> matchedCategoryNames,

        @JsonPropertyDescription("Nouvelles catégories pertinentes non présentes dans la liste existante, peut être vide")
        List<String> newCategorySuggestions,

        @JsonPropertyDescription("Prix de vente estimé en FCFA, ou null")
        Double suggestedPrice,

        @JsonPropertyDescription("Prix d'achat mentionné en FCFA, ou null")
        Double suggestedPurchasePrice,

        @JsonPropertyDescription("Quantité mentionnée, ou null")
        Integer suggestedQuantity,

        @JsonPropertyDescription("Référence produit mentionnée, ou null")
        String suggestedReference,

        @JsonPropertyDescription("Commentaire libre optionnel, ou null")
        String rawNotes
) {}