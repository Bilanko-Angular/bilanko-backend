package com.backend.bilanko.DTO.object.document;

import com.backend.bilanko.models.object.document.TypeDocument;

public record TypeDocumentDTO(
        TypeDocument code,
        String frontCode,
        String label
) {}
