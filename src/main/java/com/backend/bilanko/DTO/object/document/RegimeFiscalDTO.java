package com.backend.bilanko.DTO.object.document;

import com.backend.bilanko.models.object.document.RegimeFiscal;

public record RegimeFiscalDTO(
        RegimeFiscal code,
        String frontCode,
        String label
) {}
