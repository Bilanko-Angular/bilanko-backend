package com.backend.bilanko.DTO.object.document;

import com.backend.bilanko.models.document.RegimeFiscal;

public record RegimeFiscalDTO(
        RegimeFiscal code,
        String frontCode,
        String label
) {}
