package com.backend.bilanko.DTO.object.document;

import com.backend.bilanko.models.object.document.TypeInfoCle;

public record InfoCleDTO(
        long id,
        String slug,
        TypeInfoCle type,
        String nom,
        String information
) {}
