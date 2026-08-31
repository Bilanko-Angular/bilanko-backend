package com.backend.bilanko.DTO.object.product;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.util.List;

public record ProductRecognitionResponseDTO(
        @JsonPropertyDescription("Nom suggéré du produit, ou null si incertain")
        String suggestedName,

        @JsonPropertyDescription("Catégories parmi celles déjà existantes qui correspondent, peut être vide")
        List<String> matchedCategoryNames,

        @JsonPropertyDescription("Nouvelles catégories pertinentes non présentes dans la liste existante, peut être vide")
        List<String> newCategorySuggestions,

        @JsonPropertyDescription("Prix estimé en FCFA, ou null si tu ne peux pas estimer raisonnablement")
        Double suggestedPrice,

        @JsonPropertyDescription("Commentaire libre optionnel, ou null")
        String rawNotes
) {}