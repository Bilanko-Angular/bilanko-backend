package com.backend.bilanko.DTO.object.document;

import com.backend.bilanko.models.document.TypeDocument;

public record TypeDocumentDTO(
        TypeDocument code,
        String frontCode,
        String label
) {}
