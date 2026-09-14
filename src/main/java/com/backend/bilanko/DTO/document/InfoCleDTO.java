package com.backend.bilanko.DTO.document;

import com.backend.bilanko.models.document.TypeInfoCle;

public record InfoCleDTO(
        long id,
        String slug,
        TypeInfoCle type,
        String nom,
        String information
) {}
